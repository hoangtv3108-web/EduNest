package com.edunest.backend.xuat;

import com.edunest.backend.audit.NhatKyResponse;
import java.time.ZoneOffset;
import java.util.List;

/**
 * Cấu trúc cột chuẩn của nhật ký kiểm toán khi xuất CSV / Excel. Tên cột trùng
 * tên cột trong CSDL
 * (snake_case) để công cụ phân tích đối chiếu trực tiếp với bảng
 * nhat_ky_kiem_toan.
 */
final class CotNhatKy {

    static final List<String> TIEU_DE = List.of(
            "id", "thoi_gian", "nguoi_thuc_hien_id", "nguoi_thuc_hien_ten", "vai_tro",
            "hanh_dong", "doi_tuong_loai", "doi_tuong_id", "du_lieu_cu", "du_lieu_moi");

    private static final ZoneOffset VN = ZoneOffset.ofHours(7);

    private CotNhatKy() {
    }

    /** Thời gian ISO-8601 kèm múi giờ (vd 2026-11-19T10:15:30.123+07:00). */
    static String thoiGian(NhatKyResponse n) {
        return n.thoiGian() == null ? "" : n.thoiGian().withOffsetSameInstant(VN).toString();
    }

    static List<String> dong(NhatKyResponse n) {
        return List.of(
                str(n.id()),
                thoiGian(n),
                str(n.nguoiThucHienId()),
                str(n.nguoiThucHienTen()),
                str(n.vaiTro()),
                str(n.hanhDong()),
                str(n.doiTuongLoai()),
                str(n.doiTuongId()),
                n.duLieuCu() == null ? "" : n.duLieuCu().toString(), // JSON gọn một dòng
                n.duLieuMoi() == null ? "" : n.duLieuMoi().toString());
    }

    private static String str(Object o) {
        return o == null ? "" : o.toString();
    }
}