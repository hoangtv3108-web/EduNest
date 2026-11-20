package com.edunest.backend.nhacno;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/**
 * Truy vấn thô cho nhắc nợ. Mọi hàm phải chạy trong transaction có
 * TenantContext (RLS tự lọc theo tenant).
 * Không đặt @Transactional ở đây: NhacNoService quyết định ranh giới
 * transaction.
 */
@Repository
public class NhacNoRepository {

    private final JdbcClient jdbc;

    public NhacNoRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * Hóa đơn chưa thanh toán có hạn đóng trong [tu, den], kèm thông tin phụ huynh
     * để soạn tin.
     */
    public List<ThongTinNhac> timHoaDonDenHan(LocalDate tu, LocalDate den) {
        return jdbc.sql("""
                SELECT hd.id, tn.ten_trung_tam, ph.ho_ten AS ten_phu_huynh, ph.email, ph.sdt_zalo,
                       hv.ho_ten AS ten_hoc_vien, l.ten_lop, hd.ky_hoc_phi, hd.so_tien, hd.han_dong
                FROM hoa_don_hoc_phi hd
                JOIN ghi_danh gd ON gd.id = hd.ghi_danh_id
                JOIN hoc_vien hv ON hv.id = gd.hoc_vien_id
                JOIN lop l ON l.id = gd.lop_id
                JOIN tenant tn ON tn.id = hd.tenant_id
                LEFT JOIN phu_huynh ph ON ph.id = hv.phu_huynh_id
                WHERE hd.trang_thai = 'CHUA_THANH_TOAN'
                  AND hd.han_dong BETWEEN :tu AND :den
                ORDER BY hd.han_dong, hv.ho_ten
                """)
                .param("tu", tu)
                .param("den", den)
                .query((rs, n) -> new ThongTinNhac(
                        rs.getObject("id", UUID.class),
                        rs.getString("ten_trung_tam"),
                        rs.getString("ten_phu_huynh"),
                        rs.getString("email"),
                        rs.getString("sdt_zalo"),
                        rs.getString("ten_hoc_vien"),
                        rs.getString("ten_lop"),
                        rs.getString("ky_hoc_phi"),
                        rs.getBigDecimal("so_tien"),
                        rs.getObject("han_dong", LocalDate.class)))
                .list();
    }

    /**
     * Đã nhắc xong hoặc đang có tiến trình khác nhắc hóa đơn này qua kênh này (bất
     * kể ngày nào).
     */
    public boolean daNhacHoacDangNhac(UUID hoaDonId, KenhGui kenh) {
        Integer n = jdbc.sql("""
                SELECT count(*) FROM lich_su_nhac_no
                WHERE hoa_don_id = :hd AND kenh_gui = :kenh AND trang_thai IN ('DANG_GUI', 'DA_GUI')
                """)
                .param("hd", hoaDonId)
                .param("kenh", kenh.name())
                .query(Integer.class).single();
        return n != null && n > 0;
    }

    public int demLanLoi(UUID hoaDonId, KenhGui kenh) {
        Integer n = jdbc.sql("""
                SELECT count(*) FROM lich_su_nhac_no
                WHERE hoa_don_id = :hd AND kenh_gui = :kenh AND trang_thai = 'LOI'
                """)
                .param("hd", hoaDonId)
                .param("kenh", kenh.name())
                .query(Integer.class).single();
        return n == null ? 0 : n;
    }

    /**
     * "Giành quyền gửi": chèn dòng DANG_GUI. Ràng buộc UNIQUE (hoa_don_id,
     * kenh_gui, ngay_nhac) bảo đảm
     * chỉ MỘT tiến trình (kể cả nhiều instance) chèn được; tiến trình còn lại nhận
     * về rỗng và phải bỏ qua.
     */
    public Optional<UUID> gianhQuyenGui(UUID tenantId, UUID hoaDonId, LocalDate ngayNhac, KenhGui kenh,
            String nguoiNhan) {
        return jdbc.sql("""
                INSERT INTO lich_su_nhac_no (tenant_id, hoa_don_id, ngay_nhac, kenh_gui, trang_thai, nguoi_nhan)
                VALUES (:tenant, :hd, :ngay, :kenh, 'DANG_GUI', :nguoiNhan)
                ON CONFLICT (hoa_don_id, kenh_gui, ngay_nhac) DO NOTHING
                RETURNING id
                """)
                .param("tenant", tenantId)
                .param("hd", hoaDonId)
                .param("ngay", ngayNhac)
                .param("kenh", kenh.name())
                .param("nguoiNhan", nguoiNhan)
                .query((rs, n) -> rs.getObject(1, UUID.class))
                .optional();
    }

    public void capNhatKetQua(UUID id, String trangThai, String chiTietLoi) {
        jdbc.sql("UPDATE lich_su_nhac_no SET trang_thai = :tt, chi_tiet_loi = :loi WHERE id = :id")
                .param("tt", trangThai)
                .param("loi", chiTietLoi)
                .param("id", id)
                .update();
    }

    public List<LichSuNhacNoResponse> layLichSu(int gioiHan) {
        return jdbc.sql("""
                SELECT ls.id, ls.hoa_don_id, hv.ho_ten AS ten_hoc_vien, l.ten_lop, hd.ky_hoc_phi,
                       ls.ngay_nhac, ls.kenh_gui, ls.trang_thai, ls.nguoi_nhan, ls.chi_tiet_loi, ls.ngay_tao
                FROM lich_su_nhac_no ls
                JOIN hoa_don_hoc_phi hd ON hd.id = ls.hoa_don_id
                JOIN ghi_danh gd ON gd.id = hd.ghi_danh_id
                JOIN hoc_vien hv ON hv.id = gd.hoc_vien_id
                JOIN lop l ON l.id = gd.lop_id
                ORDER BY ls.ngay_tao DESC
                LIMIT :gh
                """)
                .param("gh", gioiHan)
                .query((rs, n) -> new LichSuNhacNoResponse(
                        rs.getObject("id", UUID.class),
                        rs.getObject("hoa_don_id", UUID.class),
                        rs.getString("ten_hoc_vien"),
                        rs.getString("ten_lop"),
                        rs.getString("ky_hoc_phi"),
                        rs.getObject("ngay_nhac", LocalDate.class),
                        KenhGui.valueOf(rs.getString("kenh_gui")),
                        rs.getString("trang_thai"),
                        rs.getString("nguoi_nhan"),
                        rs.getString("chi_tiet_loi"),
                        rs.getObject("ngay_tao", java.time.OffsetDateTime.class)))
                .list();
    }
}