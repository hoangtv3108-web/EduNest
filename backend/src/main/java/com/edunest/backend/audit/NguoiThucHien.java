package com.edunest.backend.audit;

import java.util.UUID;

/**
 * Người đang thao tác, lấy từ JWT đã được Spring Security xác thực (không tin
 * dữ liệu client gửi lên).
 */
public record NguoiThucHien(UUID id, String ten, String vaiTro) {

    public static final NguoiThucHien HE_THONG = new NguoiThucHien(null, "HỆ THỐNG", null);
}