package com.edunest.backend.baocao;

import com.edunest.backend.util.ThoiGianVN;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.UUID;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Báo cáo tổng hợp cuối tháng. Không có WHERE tenant_id: RLS ở PostgreSQL tự
 * giới hạn theo trung tâm
 * của người gọi (TenantContext được gắn vào transaction bởi
 * TenantAwareTransactionManager).
 */
@Service
public class BaoCaoService {

    private final JdbcClient jdbc;

    public BaoCaoService(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    /** "2026-11" -> YearMonth; null/rỗng -> tháng hiện tại (giờ VN). */
    public static YearMonth parseThang(String thang) {
        if (thang == null || thang.isBlank()) {
            return YearMonth.from(ThoiGianVN.homNay());
        }
        try {
            return YearMonth.parse(thang.trim());
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Tháng không hợp lệ, định dạng đúng là yyyy-MM (ví dụ 2026-11)");
        }
    }

    @Transactional(readOnly = true)
    public DoanhThuThangResponse doanhThuThang(YearMonth thang) {
        LocalDate tu = thang.atDay(1);
        LocalDate den = thang.atEndOfMonth();

        return jdbc.sql("""
                SELECT count(*)                                                        AS so_hoa_don,
                       COALESCE(sum(so_tien), 0)                                       AS tong_phat_sinh,
                       COALESCE(sum(so_tien) FILTER (WHERE trang_thai = 'DA_THANH_TOAN'), 0) AS tong_da_thu,
                       count(*) FILTER (WHERE trang_thai = 'DA_THANH_TOAN')            AS so_da_thu,
                       count(*) FILTER (WHERE trang_thai <> 'DA_THANH_TOAN' AND han_dong < :homNay) AS so_qua_han,
                       count(*) FILTER (WHERE trang_thai = 'DA_THANH_TOAN'
                                          AND ngay_thanh_toan IS NOT NULL
                                          AND ngay_thanh_toan <= han_dong)             AS so_dung_han
                FROM hoa_don_hoc_phi
                WHERE han_dong BETWEEN :tu AND :den
                """)
                .param("tu", tu)
                .param("den", den)
                .param("homNay", ThoiGianVN.homNay())
                .query((rs, n) -> {
                    long soHoaDon = rs.getLong("so_hoa_don");
                    BigDecimal phatSinh = rs.getBigDecimal("tong_phat_sinh");
                    BigDecimal daThu = rs.getBigDecimal("tong_da_thu");
                    long soDaThu = rs.getLong("so_da_thu");
                    long soDungHan = rs.getLong("so_dung_han");
                    return new DoanhThuThangResponse(
                            thang.toString(),
                            soHoaDon,
                            phatSinh,
                            daThu,
                            phatSinh.subtract(daThu),
                            soDaThu,
                            soHoaDon - soDaThu,
                            rs.getLong("so_qua_han"),
                            soDungHan,
                            tiLe(daThu, phatSinh),
                            tiLe(BigDecimal.valueOf(soDungHan), BigDecimal.valueOf(soHoaDon)));
                })
                .single();
    }

    @Transactional(readOnly = true)
    public ChuyenCanThangResponse chuyenCanThang(YearMonth thang) {
        LocalDate tu = thang.atDay(1);
        LocalDate den = thang.atEndOfMonth();

        List<ChuyenCanLopResponse> theoLop = jdbc.sql("""
                SELECT l.id AS lop_id, l.ten_lop,
                       count(DISTINCT b.id)                                         AS so_buoi_day,
                       count(DISTINCT b.id) FILTER (WHERE d.id IS NOT NULL)         AS so_buoi_da_diem_danh,
                       count(d.id)                                                  AS tong_luot,
                       count(d.id) FILTER (WHERE d.trang_thai = 'CO_MAT')           AS co_mat,
                       count(d.id) FILTER (WHERE d.trang_thai = 'CO_PHEP')          AS vang_co_phep,
                       count(d.id) FILTER (WHERE d.trang_thai = 'VANG')             AS vang_khong_phep
                FROM lop l
                JOIN buoi_hoc b ON b.lop_id = l.id AND b.ngay_hoc BETWEEN :tu AND :den
                LEFT JOIN diem_danh d ON d.buoi_hoc_id = b.id
                GROUP BY l.id, l.ten_lop
                ORDER BY l.ten_lop
                """)
                .param("tu", tu)
                .param("den", den)
                .query((rs, n) -> {
                    long tong = rs.getLong("tong_luot");
                    long coMat = rs.getLong("co_mat");
                    long coPhep = rs.getLong("vang_co_phep");
                    long khongPhep = rs.getLong("vang_khong_phep");
                    return new ChuyenCanLopResponse(
                            rs.getObject("lop_id", UUID.class),
                            rs.getString("ten_lop"),
                            rs.getInt("so_buoi_day"),
                            rs.getInt("so_buoi_da_diem_danh"),
                            tong, coMat, coPhep, khongPhep,
                            tiLe(coMat, tong), tiLe(coPhep, tong), tiLe(khongPhep, tong));
                })
                .list();

        int tongBuoi = 0, tongBuoiDd = 0;
        long tong = 0, coMat = 0, coPhep = 0, khongPhep = 0;
        for (ChuyenCanLopResponse l : theoLop) {
            tongBuoi += l.soBuoiDay();
            tongBuoiDd += l.soBuoiDaDiemDanh();
            tong += l.tongLuotDiemDanh();
            coMat += l.coMat();
            coPhep += l.vangCoPhep();
            khongPhep += l.vangKhongPhep();
        }
        return new ChuyenCanThangResponse(thang.toString(), tongBuoi, tongBuoiDd, tong, coMat, coPhep, khongPhep,
                tiLe(coMat, tong), tiLe(coPhep, tong), tiLe(khongPhep, tong), theoLop);
    }

    /** Chi tiết từng hóa đơn có hạn đóng trong tháng (dùng cho báo cáo Excel). */
    @Transactional(readOnly = true)
    public List<DongHoaDonBaoCao> chiTietHoaDon(YearMonth thang) {
        return jdbc.sql("""
                SELECT hv.ho_ten AS ten_hoc_vien, l.ten_lop, ph.ho_ten AS ten_phu_huynh, hd.ky_hoc_phi,
                       hd.so_buoi, hd.so_tien, hd.han_dong, hd.trang_thai, hd.ngay_thanh_toan
                FROM hoa_don_hoc_phi hd
                JOIN ghi_danh gd ON gd.id = hd.ghi_danh_id
                JOIN hoc_vien hv ON hv.id = gd.hoc_vien_id
                JOIN lop l ON l.id = gd.lop_id
                LEFT JOIN phu_huynh ph ON ph.id = hv.phu_huynh_id
                WHERE hd.han_dong BETWEEN :tu AND :den
                ORDER BY l.ten_lop, hv.ho_ten, hd.han_dong
                """)
                .param("tu", thang.atDay(1))
                .param("den", thang.atEndOfMonth())
                .query((rs, n) -> new DongHoaDonBaoCao(
                        rs.getString("ten_hoc_vien"),
                        rs.getString("ten_lop"),
                        rs.getString("ten_phu_huynh"),
                        rs.getString("ky_hoc_phi"),
                        rs.getInt("so_buoi"),
                        rs.getBigDecimal("so_tien"),
                        rs.getObject("han_dong", LocalDate.class),
                        rs.getString("trang_thai"),
                        rs.getObject("ngay_thanh_toan", LocalDate.class)))
                .list();
    }

    /** Tên trung tâm hiện tại (RLS chỉ còn đúng một hàng). */
    @Transactional(readOnly = true)
    public String tenTrungTam() {
        return jdbc.sql("SELECT ten_trung_tam FROM tenant").query(String.class).optional().orElse("EduNest");
    }

    private static BigDecimal tiLe(long tu, long mau) {
        return tiLe(BigDecimal.valueOf(tu), BigDecimal.valueOf(mau));
    }

    /** Phần trăm làm tròn 1 chữ số; mẫu = 0 thì trả 0.0 (không chia cho 0). */
    private static BigDecimal tiLe(BigDecimal tu, BigDecimal mau) {
        if (mau.signum() == 0)
            return BigDecimal.ZERO.setScale(1);
        return tu.multiply(BigDecimal.valueOf(100)).divide(mau, 1, RoundingMode.HALF_UP);
    }
}