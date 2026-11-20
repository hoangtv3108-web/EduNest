package com.edunest.backend.nhacno;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Cấu hình nhắc học phí (tiền tố edunest.nhac-no). Mặc định: 8h sáng hằng ngày,
 * nhắc trước hạn 2-3 ngày,
 * chỉ gửi Email. Khi chưa cấu hình SMTP / webhook Zalo thì hệ thống chạy chế độ
 * giả lập (chỉ ghi log).
 */
@ConfigurationProperties(prefix = "edunest.nhac-no")
public record NhacNoProperties(
        @DefaultValue("true") boolean batTuDong,
        @DefaultValue("0 0 8 * * *") String cron,
        @DefaultValue("2") int soNgayTruocToiThieu,
        @DefaultValue("3") int soNgayTruocToiDa,
        @DefaultValue("3") int soLanThuLaiToiDa,
        @DefaultValue("EMAIL") List<KenhGui> kenhGui,
        @DefaultValue Email email,
        @DefaultValue Zalo zalo) {

    public record Email(@DefaultValue("no-reply@edunest.local") String tuDiaChi) {
    }

    /**
     * webhookUrl rỗng = giả lập. Với Zalo ZNS thật:
     * https://business.openapi.zalo.me/message/template
     */
    public record Zalo(String webhookUrl, String accessToken, String templateId) {
    }
}