package com.edunest.backend.nhacno;

import com.edunest.backend.util.CheDuLieu;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Gửi Zalo ZNS qua webhook/API (định dạng theo Zalo ZNS: phone, template_id,
 * template_data, tracking_id).
 * Chưa cấu hình edunest.nhac-no.zalo.webhook-url thì chạy giả lập (ghi log).
 */
@Component
public class ZaloZnsKenhNhacNo implements KenhNhacNo {

    private static final Logger log = LoggerFactory.getLogger(ZaloZnsKenhNhacNo.class);

    private final NhacNoProperties props;
    private final RestClient http = RestClient.create();

    public ZaloZnsKenhNhacNo(NhacNoProperties props) {
        this.props = props;
    }

    @Override
    public KenhGui kenh() {
        return KenhGui.ZALO_ZNS;
    }

    @Override
    public Optional<String> nguoiNhanDaChe(ThongTinNhac t) {
        return chuanHoaSdt(t.sdtZalo()).map(CheDuLieu::soDienThoai);
    }

    @Override
    public void gui(ThongTinNhac t) {
        String sdt = chuanHoaSdt(t.sdtZalo())
                .orElseThrow(() -> new IllegalStateException("Số Zalo không hợp lệ"));
        String url = props.zalo().webhookUrl();
        if (url == null || url.isBlank()) {
            log.info("[GIẢ LẬP ZALO ZNS] hoaDon={} -> {} | {} {} hạn {}",
                    t.hoaDonId(), CheDuLieu.soDienThoai(sdt), t.tenHocVien(), t.soTienHienThi(), t.hanDongHienThi());
            return;
        }

        Map<String, Object> templateData = new LinkedHashMap<>();
        templateData.put("ten_phu_huynh", nvl(t.tenPhuHuynh()));
        templateData.put("ten_hoc_vien", t.tenHocVien());
        templateData.put("ten_lop", t.tenLop());
        templateData.put("ky_hoc_phi", t.kyHocPhi());
        templateData.put("so_tien", t.soTienHienThi());
        templateData.put("han_dong", t.hanDongHienThi());

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("phone", sdt);
        body.put("template_id", props.zalo().templateId());
        body.put("template_data", templateData);
        body.put("tracking_id", t.hoaDonId().toString());

        RestClient.RequestBodySpec req = http.post().uri(url).header("Content-Type", "application/json");
        if (props.zalo().accessToken() != null && !props.zalo().accessToken().isBlank()) {
            req = req.header("access_token", props.zalo().accessToken());
        }
        JsonNode res = req.body(body).retrieve().body(JsonNode.class);
        // Zalo trả HTTP 200 kèm {"error": <mã>, "message": ...}; error != 0 là thất
        // bại.
        if (res != null && res.hasNonNull("error") && res.get("error").asInt() != 0) {
            throw new IllegalStateException("Zalo ZNS từ chối: mã lỗi " + res.get("error").asInt());
        }
    }

    /**
     * 0912345678 / +84 912 345 678 -> 84912345678; trả rỗng nếu không giống số di
     * động VN.
     */
    static Optional<String> chuanHoaSdt(String raw) {
        if (raw == null)
            return Optional.empty();
        String s = raw.replaceAll("[^0-9]", "");
        if (s.startsWith("0"))
            s = "84" + s.substring(1);
        if (!s.startsWith("84") || s.length() < 11 || s.length() > 12)
            return Optional.empty();
        return Optional.of(s);
    }

    private static String nvl(String s) {
        return s == null ? "" : s;
    }
}