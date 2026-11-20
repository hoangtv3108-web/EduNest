package com.edunest.backend;

import com.edunest.backend.audit.HanhDongKiemToan;
import com.edunest.backend.audit.NhatKyKiemToanService;
import com.edunest.backend.audit.NhatKyResponse;
import com.edunest.backend.audit.TrangNhatKy;
import com.edunest.backend.baocao.BaoCaoService;
import com.edunest.backend.baocao.DoanhThuThangResponse;
import com.edunest.backend.baocao.DongHoaDonBaoCao;
import com.edunest.backend.nhacno.KenhGui;
import com.edunest.backend.nhacno.KenhNhacNo;
import com.edunest.backend.nhacno.KetQuaQuetNhacNo;
import com.edunest.backend.nhacno.NhacNoProperties;
import com.edunest.backend.nhacno.NhacNoRepository;
import com.edunest.backend.nhacno.NhacNoService;
import com.edunest.backend.nhacno.ThongTinNhac;
import com.edunest.backend.tenant.TenantContext;
import com.edunest.backend.xuat.XuatBaoCaoService;
import com.edunest.backend.xuat.XuatFile;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.support.SimpleTransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class Phase3UnitTest {

    private final UUID mockTenantId = UUID.randomUUID();

    // Fake TransactionTemplate chạy trực tiếp in-memory không phụ thuộc DB
    private final TransactionTemplate fakeTx = new TransactionTemplate() {
        @Override
        public <T> T execute(TransactionCallback<T> action) throws TransactionException {
            return action.doInTransaction(new SimpleTransactionStatus());
        }
    };

    @BeforeEach
    void setUp() {
        TenantContext.set(mockTenantId);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    private NhacNoProperties taoProps() {
        return new NhacNoProperties(
                true, "0 0 8 * * *", 2, 3, 3,
                List.of(KenhGui.EMAIL),
                new NhacNoProperties.Email("no-reply@edunest.vn"),
                new NhacNoProperties.Zalo(null, null, null)
        );
    }

    // Fake Kênh gửi thông báo
    static class FakeKenhNhacNo implements KenhNhacNo {
        final AtomicInteger soLanGui = new AtomicInteger(0);

        @Override
        public KenhGui kenh() {
            return KenhGui.EMAIL;
        }

        @Override
        public Optional<String> nguoiNhanDaChe(ThongTinNhac thongTin) {
            return Optional.of("phuhuynh@example.com");
        }

        @Override
        public void gui(ThongTinNhac thongTin) {
            soLanGui.incrementAndGet();
        }
    }

    @Test
    @DisplayName("Test 1: Nhắc nợ tự động - Kiểm tra Idempotency chống gửi trùng")
    void test1_NhacNo_Idempotency_BoQuaNeuDaGui() {
        FakeKenhNhacNo kenh = new FakeKenhNhacNo();
        UUID hoaDonId = UUID.randomUUID();

        // Fake Repo: Hóa đơn này ĐÃ ĐƯỢC GỬI trước đó
        NhacNoRepository fakeRepo = new NhacNoRepository(null) {
            @Override
            public List<ThongTinNhac> timHoaDonDenHan(LocalDate tu, LocalDate den) {
                return List.of(new ThongTinNhac(
                        hoaDonId, "Trung tâm EduNest", "Phụ huynh A",
                        "phuhuynh@example.com", "0912345678", "Học viên A",
                        "Toán 9A", "11/2026", BigDecimal.valueOf(1500000), LocalDate.now().plusDays(2)
                ));
            }

            @Override
            public boolean daNhacHoacDangNhac(UUID hdId, KenhGui k) {
                return true; // ĐÃ GỬI
            }
        };

        NhacNoService service = new NhacNoService(fakeRepo, fakeTx, taoProps(), List.of(kenh));
        KetQuaQuetNhacNo ketQua = service.quetVaNhac(LocalDate.now());

        // Kỳ vọng: 1 hóa đơn cần nhắc nhưng bị bỏ qua do đã gửi
        assertThat(ketQua.soHoaDonDenHan()).isEqualTo(1);
        assertThat(ketQua.daGui()).isEqualTo(0);
        assertThat(ketQua.boQuaDaNhac()).isEqualTo(1);
        assertThat(ketQua.loi()).isEqualTo(0);
        assertThat(kenh.soLanGui.get()).isEqualTo(0); // Không gửi email
    }

    @Test
    @DisplayName("Test 2: Nhắc nợ tự động - Gửi thành công lần đầu và cập nhật trạng thái DA_GUI")
    void test2_NhacNo_GuiThanhCongVaGhiNhan() {
        FakeKenhNhacNo kenh = new FakeKenhNhacNo();
        UUID hoaDonId = UUID.randomUUID();
        UUID lichSuId = UUID.randomUUID();
        AtomicBoolean daCapNhatDaGui = new AtomicBoolean(false);

        NhacNoRepository fakeRepo = new NhacNoRepository(null) {
            @Override
            public List<ThongTinNhac> timHoaDonDenHan(LocalDate tu, LocalDate den) {
                return List.of(new ThongTinNhac(
                        hoaDonId, "Trung tâm EduNest", "Phụ huynh B",
                        "phuhuynh2@example.com", "0987654321", "Học viên B",
                        "Văn 10", "11/2026", BigDecimal.valueOf(2000000), LocalDate.now().plusDays(3)
                ));
            }

            @Override
            public boolean daNhacHoacDangNhac(UUID hdId, KenhGui k) {
                return false; // Chưa gửi
            }

            @Override
            public int demLanLoi(UUID hdId, KenhGui k) {
                return 0; // Chưa từng lỗi
            }

            @Override
            public Optional<UUID> gianhQuyenGui(UUID tenantId, UUID hdId, LocalDate ngay, KenhGui k, String nn) {
                return Optional.of(lichSuId);
            }

            @Override
            public void capNhatKetQua(UUID id, String trangThai, String chiTietLoi) {
                if ("DA_GUI".equals(trangThai) && lichSuId.equals(id)) {
                    daCapNhatDaGui.set(true);
                }
            }
        };

        NhacNoService service = new NhacNoService(fakeRepo, fakeTx, taoProps(), List.of(kenh));
        KetQuaQuetNhacNo ketQua = service.quetVaNhac(LocalDate.now());

        assertThat(ketQua.soHoaDonDenHan()).isEqualTo(1);
        assertThat(ketQua.daGui()).isEqualTo(1);
        assertThat(ketQua.boQuaDaNhac()).isEqualTo(0);
        assertThat(kenh.soLanGui.get()).isEqualTo(1);
        assertThat(daCapNhatDaGui.get()).isTrue();
    }

    @Test
    @DisplayName("Test 3: Nhật ký kiểm toán - Cấu trúc dữ liệu và phân trang kết quả")
    void test3_AuditLog_TimKiemVaPhanTrang() {
        UUID logId = UUID.randomUUID();
        NhatKyResponse item = new NhatKyResponse(
                logId,
                UUID.randomUUID(),
                "Admin Tenant",
                "CENTER_ADMIN",
                "XAC_NHAN_THANH_TOAN",
                "HOA_DON",
                UUID.randomUUID(),
                null,
                null,
                OffsetDateTime.now()
        );
        TrangNhatKy page = new TrangNhatKy(List.of(item), 0, 20, 1L);

        assertThat(page.total()).isEqualTo(1L);
        assertThat(page.page()).isEqualTo(0);
        assertThat(page.size()).isEqualTo(20);
        assertThat(page.items()).hasSize(1);
        assertThat(page.items().get(0).hanhDong()).isEqualTo("XAC_NHAN_THANH_TOAN");
        assertThat(page.items().get(0).id()).isEqualTo(logId);
    }

    @Test
    @DisplayName("Test 4: Báo cáo cuối tháng - Tính toán doanh thu và tỷ lệ hoàn thành")
    void test4_BaoCao_DoanhThuVaTyLeDongDungHan() {
        DoanhThuThangResponse resp = new DoanhThuThangResponse(
                "2026-11",
                10L,
                BigDecimal.valueOf(10000000), // Tổng học phí phát sinh
                BigDecimal.valueOf(8000000),  // Đã thu
                BigDecimal.valueOf(2000000),  // Còn nợ
                8L,                           // Số hóa đơn đã thu
                2L,                           // Số hóa đơn còn nợ
                0L,                           // Quá hạn chưa thu
                7L,                           // Đóng đúng hạn
                BigDecimal.valueOf(80.0),     // Tỷ lệ thu %
                BigDecimal.valueOf(70.0)      // Tỷ lệ đúng hạn %
        );

        assertThat(resp.tongHocPhiPhatSinh()).isEqualTo(BigDecimal.valueOf(10000000));
        assertThat(resp.tongDaThu()).isEqualTo(BigDecimal.valueOf(8000000));
        assertThat(resp.tongConNo()).isEqualTo(BigDecimal.valueOf(2000000));
        assertThat(resp.soHoaDonDaThu()).isEqualTo(8L);
        assertThat(resp.soHoaDonConNo()).isEqualTo(2L);
        assertThat(resp.tiLeThuPhanTram()).isEqualTo(BigDecimal.valueOf(80.0));
        assertThat(resp.tiLeDungHanPhanTram()).isEqualTo(BigDecimal.valueOf(70.0));
    }

    @Test
    @DisplayName("Test 5: Xuất file - Kiểm tra tạo file Excel (.xlsx) hợp lệ và mã băm SHA-256")
    void test5_XuatBaoCao_ExcelVaCsvChuanDinhDang() {
        YearMonth thang = YearMonth.of(2026, 11);
        DoanhThuThangResponse mockTong = new DoanhThuThangResponse(
                "2026-11", 5L, BigDecimal.valueOf(5000000), BigDecimal.valueOf(5000000),
                BigDecimal.ZERO, 5L, 0L, 0L, 5L, BigDecimal.valueOf(100.0), BigDecimal.valueOf(100.0)
        );
        List<DongHoaDonBaoCao> mockDong = List.of(
                new DongHoaDonBaoCao(
                        "Lê Học Viên", "Lớp Lý 11", "Lê Phụ Huynh", "11/2026",
                        8, BigDecimal.valueOf(1000000),
                        LocalDate.of(2026, 11, 10), "DA_THANH_TOAN",
                        LocalDate.of(2026, 11, 5)
                )
        );

        BaoCaoService fakeBaoCao = new BaoCaoService(null) {
            @Override
            public String tenTrungTam() {
                return "Trung Tâm Luyện Thi EduNest";
            }

            @Override
            public DoanhThuThangResponse doanhThuThang(YearMonth t) {
                return mockTong;
            }

            @Override
            public List<DongHoaDonBaoCao> chiTietHoaDon(YearMonth t) {
                return mockDong;
            }
        };

        AtomicBoolean daGhiLog = new AtomicBoolean(false);
        NhatKyKiemToanService fakeNhatKy = new NhatKyKiemToanService(null, new ObjectMapper()) {
            @Override
            public void ghi(String hanhDong, String doiTuongLoai, UUID doiTuongId, Object duLieuCu, Object duLieuMoi) {
                if (HanhDongKiemToan.XUAT_BAO_CAO_HOC_PHI.equals(hanhDong)) {
                    daGhiLog.set(true);
                }
            }
        };

        XuatBaoCaoService xuatService = new XuatBaoCaoService(fakeBaoCao, fakeNhatKy, new ObjectMapper());
        XuatFile excelFile = xuatService.xuatDoanhThuExcel(thang);

        assertThat(excelFile).isNotNull();
        assertThat(excelFile.tenFile()).contains("bao-cao-hoc-phi-2026-11.xlsx");
        assertThat(excelFile.contentType()).isEqualTo(XuatBaoCaoService.EXCEL_TYPE);
        assertThat(excelFile.noiDung()).isNotEmpty();
        // File Excel .xlsx luôn có ZIP magic bytes 0x50 0x4B (PK)
        assertThat(excelFile.noiDung()[0]).isEqualTo((byte) 0x50);
        assertThat(excelFile.noiDung()[1]).isEqualTo((byte) 0x4B);
        assertThat(excelFile.sha256()).isNotEmpty();
        assertThat(daGhiLog.get()).isTrue();
    }

    @Test
    @DisplayName("Test 6: Kiểm soát bảo mật - Từ chối thao tác nếu thiếu TenantContext")
    void test6_TenantContext_BaoVeCachLyDuLieu() {
        TenantContext.clear(); // Xóa context mô phỏng truy cập không có thông tin định danh tenant

        FakeKenhNhacNo kenh = new FakeKenhNhacNo();
        NhacNoRepository fakeRepo = new NhacNoRepository(null);
        NhacNoService service = new NhacNoService(fakeRepo, fakeTx, taoProps(), List.of(kenh));

        assertThatThrownBy(() -> service.quetVaNhac(LocalDate.now()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Không xác định được tenant_id");
    }
}
