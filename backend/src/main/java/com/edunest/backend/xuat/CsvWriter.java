package com.edunest.backend.xuat;

import java.util.List;

/**
 * Ghi CSV theo RFC 4180: ngăn cách bằng dấu phẩy, xuống dòng CRLF, ô chứa dấu
 * phẩy / nháy kép / xuống dòng
 * được bọc trong nháy kép (nháy kép bên trong nhân đôi).
 *
 * Chống "CSV injection": ô văn bản bắt đầu bằng = + - @ (hoặc tab / CR) sẽ bị
 * Excel hiểu là công thức,
 * nên được thêm dấu nháy đơn ở đầu. Chỉ ảnh hưởng ô văn bản tự do (vd: tên
 * người dùng); UUID, thời gian
 * và JSON không bắt đầu bằng các ký tự này nên giữ nguyên.
 */
public final class CsvWriter {

    private static final String CRLF = "\r\n";

    private CsvWriter() {
    }

    public static String dong(List<String> cacO) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < cacO.size(); i++) {
            if (i > 0)
                sb.append(',');
            sb.append(thoat(cacO.get(i)));
        }
        return sb.append(CRLF).toString();
    }

    static String thoat(String giaTri) {
        if (giaTri == null || giaTri.isEmpty())
            return "";
        String s = chongCongThuc(giaTri);
        boolean canBoc = s.indexOf(',') >= 0 || s.indexOf('"') >= 0 || s.indexOf('\n') >= 0 || s.indexOf('\r') >= 0;
        return canBoc ? "\"" + s.replace("\"", "\"\"") + "\"" : s;
    }

    static String chongCongThuc(String s) {
        char c = s.charAt(0);
        return (c == '=' || c == '+' || c == '-' || c == '@' || c == '\t' || c == '\r') ? "'" + s : s;
    }
}