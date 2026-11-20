package com.edunest.backend.util;

import java.time.LocalDate;
import java.time.ZoneId;

/**
 * Múi giờ nghiệp vụ của hệ thống (trung tâm tại Việt Nam): dùng cho "hôm nay",
 * ngày thanh toán, báo cáo tháng.
 */
public final class ThoiGianVN {

    public static final ZoneId MUI_GIO = ZoneId.of("Asia/Ho_Chi_Minh");

    private ThoiGianVN() {
    }

    public static LocalDate homNay() {
        return LocalDate.now(MUI_GIO);
    }
}