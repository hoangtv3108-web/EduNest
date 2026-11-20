package com.edunest.backend.util;

/**
 * Che bớt dữ liệu cá nhân trước khi ghi vào log / bảng lịch sử (không lưu
 * nguyên email, số điện thoại).
 */
public final class CheDuLieu {

    private CheDuLieu() {
    }

    /** phuhuynh@gmail.com -> ph***@gmail.com */
    public static String email(String email) {
        if (email == null || email.isBlank())
            return null;
        int at = email.indexOf('@');
        if (at <= 0)
            return "***";
        String local = email.substring(0, at);
        String giuLai = local.length() <= 2 ? local.substring(0, 1) : local.substring(0, 2);
        return giuLai + "***" + email.substring(at);
    }

    /** 0912345678 -> 09*****678 */
    public static String soDienThoai(String sdt) {
        if (sdt == null || sdt.isBlank())
            return null;
        String s = sdt.replaceAll("\\s+", "");
        if (s.length() <= 5)
            return "***";
        return s.substring(0, 2) + "*".repeat(s.length() - 5) + s.substring(s.length() - 3);
    }
}