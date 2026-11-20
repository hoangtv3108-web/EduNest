package com.edunest.backend.nhacno;

import java.util.Optional;

/**
 * Một kênh gửi thông báo (Email, Zalo ZNS...). Thêm kênh mới = thêm một bean
 * implement interface này.
 */
public interface KenhNhacNo {

    KenhGui kenh();

    /**
     * Địa chỉ nhận đã che bớt để lưu lịch sử; rỗng nếu phụ huynh chưa có thông tin
     * liên lạc cho kênh này.
     */
    Optional<String> nguoiNhanDaChe(ThongTinNhac thongTin);

    /**
     * Gửi thật (hoặc giả lập). Ném RuntimeException nếu thất bại; không được nuốt
     * lỗi.
     */
    void gui(ThongTinNhac thongTin);
}