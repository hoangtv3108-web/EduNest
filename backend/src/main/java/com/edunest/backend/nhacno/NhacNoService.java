package com.edunest.backend.nhacno;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import com.edunest.backend.tenant.TenantContext;

/**
 * Quét hóa đơn sắp đến hạn của MỘT trung tâm và gửi nhắc, có chống gửi trùng.
 *
 * Quy tắc chống gửi trùng (theo hóa đơn + kênh; một hóa đơn tương ứng đúng một
 * kỳ học phí):
 * 1. Đã có lần nhắc DA_GUI/DANG_GUI -> bỏ qua (dù quét lại hằng ngày trong cửa
 * sổ 2-3 ngày).
 * 2. Từng LOI -> được thử lại ở các ngày sau, tối đa soLanThuLaiToiDa lần.
 * 3. Chốt chặn ở CSDL: UNIQUE (hoa_don_id, kenh_gui, ngay_nhac) + INSERT ... ON
 * CONFLICT DO NOTHING,
 * nên nhiều instance chạy cùng lúc cũng chỉ gửi một lần.
 *
 * Gọi từ luồng đã có TenantContext (request của quản lý, hoặc NhacNoScheduler
 * cho từng tenant).
 */
@Service
public class NhacNoService {

    private static final Logger log = LoggerFactory.getLogger(NhacNoService.class);

    private final NhacNoRepository repo;
    private final TransactionTemplate tx;
    private final NhacNoProperties props;
    private final Map<KenhGui, KenhNhacNo> kenhs;

    public NhacNoService(NhacNoRepository repo, TransactionTemplate tx, NhacNoProperties props,
            List<KenhNhacNo> kenhs) {
        this.repo = repo;
        this.tx = tx;
        this.props = props;
        this.kenhs = kenhs.stream().collect(Collectors.toMap(KenhNhacNo::kenh, Function.identity()));
    }

    public KetQuaQuetNhacNo quetVaNhac(LocalDate homNay) {
        UUID tenantId = TenantContext.get();
        if (tenantId == null) {
            throw new IllegalStateException("Không xác định được tenant_id của phiên làm việc");
        }
        LocalDate tu = homNay.plusDays(props.soNgayTruocToiThieu());
        LocalDate den = homNay.plusDays(props.soNgayTruocToiDa());

        List<ThongTinNhac> canNhac = tx.execute(s -> repo.timHoaDonDenHan(tu, den));
        int daGui = 0, boQuaDaNhac = 0, boQuaKhongCoLienLac = 0, loi = 0;

        for (ThongTinNhac t : canNhac) {
            for (KenhGui kenhGui : props.kenhGui()) {
                KenhNhacNo kenh = kenhs.get(kenhGui);
                if (kenh == null)
                    continue;
                Optional<String> nguoiNhan = kenh.nguoiNhanDaChe(t);
                if (nguoiNhan.isEmpty()) {
                    boQuaKhongCoLienLac++;
                    continue;
                }
                try {
                    switch (xuLyMotKenh(tenantId, homNay, t, kenh, nguoiNhan.get())) {
                        case DA_GUI -> daGui++;
                        case BO_QUA -> boQuaDaNhac++;
                        case LOI -> loi++;
                    }
                } catch (RuntimeException e) {
                    loi++;
                    log.error("Nhắc nợ thất bại ngoài dự kiến: hoaDon={} kenh={}", t.hoaDonId(), kenhGui, e);
                }
            }
        }
        KetQuaQuetNhacNo kq = new KetQuaQuetNhacNo(canNhac.size(), daGui, boQuaDaNhac, boQuaKhongCoLienLac, loi);
        log.info("Quét nhắc nợ tenant={} ngày={}: {}", tenantId, homNay, kq);
        return kq;
    }

    private enum KetQuaMotKenh {
        DA_GUI, BO_QUA, LOI
    }

    /**
     * Mỗi (hóa đơn, kênh) một transaction riêng: lỗi ở hóa đơn này không kéo theo
     * các hóa đơn khác.
     */
    private KetQuaMotKenh xuLyMotKenh(UUID tenantId, LocalDate homNay, ThongTinNhac t, KenhNhacNo kenh,
            String nguoiNhan) {
        return tx.execute(status -> {
            if (repo.daNhacHoacDangNhac(t.hoaDonId(), kenh.kenh()))
                return KetQuaMotKenh.BO_QUA;
            if (repo.demLanLoi(t.hoaDonId(), kenh.kenh()) >= props.soLanThuLaiToiDa())
                return KetQuaMotKenh.BO_QUA;

            Optional<UUID> luot = repo.gianhQuyenGui(tenantId, t.hoaDonId(), homNay, kenh.kenh(), nguoiNhan);
            if (luot.isEmpty())
                return KetQuaMotKenh.BO_QUA; // tiến trình khác (hoặc lần chạy trước trong ngày) đã giành

            try {
                kenh.gui(t);
                repo.capNhatKetQua(luot.get(), "DA_GUI", null);
                return KetQuaMotKenh.DA_GUI;
            } catch (RuntimeException e) {
                log.warn("Gửi nhắc nợ lỗi: hoaDon={} kenh={}: {}", t.hoaDonId(), kenh.kenh(), e.toString());
                repo.capNhatKetQua(luot.get(), "LOI", cat(e.getClass().getSimpleName() + ": " + e.getMessage()));
                return KetQuaMotKenh.LOI;
            }
        });
    }

    @Transactional(readOnly = true)
    public List<LichSuNhacNoResponse> layLichSu(int gioiHan) {
        return repo.layLichSu(Math.max(1, Math.min(gioiHan, 200)));
    }

    private static String cat(String s) {
        return s.length() <= 500 ? s : s.substring(0, 500);
    }
}