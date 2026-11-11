package com.edunest.backend.keycloak;

import com.fasterxml.jackson.databind.JsonNode;
import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

/**
 * Gọi Admin REST API của Keycloak bằng client "edunest-backend" (client_credentials, service account).
 * Chỉ dùng cho việc tạo/xoá người dùng quản trị lúc đăng ký trung tâm.
 */
@Component
public class KeycloakAdminClient {

    private static final Logger log = LoggerFactory.getLogger(KeycloakAdminClient.class);
    private static final String ROLE_QUAN_TRI = "CENTER_ADMIN";

    private final KeycloakProperties props;
    private final RestClient http;

    public KeycloakAdminClient(KeycloakProperties props) {
        this.props = props;
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(5));
        factory.setReadTimeout(Duration.ofSeconds(10));
        this.http = RestClient.builder().baseUrl(props.baseUrl()).requestFactory(factory).build();
    }

    /**
     * Tạo người dùng quản trị, gắn tenant_id và vai trò CENTER_ADMIN. Trả về id (claim "sub") của người dùng.
     * Ném 409 nếu email đã tồn tại, 502 nếu Keycloak lỗi.
     */
    public UUID taoQuanTriTrungTam(String email, String hoTen, String matKhau, UUID tenantId) {
        UUID userId = null;
        try {
            String token = layTokenQuanTri();
            userId = taoNguoiDung(token, email, hoTen, matKhau, tenantId);
            ganVaiTro(token, userId, ROLE_QUAN_TRI);
            return userId;
        } catch (ResponseStatusException e) {
            throw e; // 409 đã được xử lý ở taoNguoiDung
        } catch (RestClientException e) {
            log.error("Keycloak Admin API lỗi: {}", e.getMessage());
            xoaNguoiDungNeuCo(userId); // dọn nếu đã lỡ tạo user mà gán vai trò thất bại
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Không tạo được tài khoản trên hệ thống định danh");
        }
    }

    /** Xoá người dùng để bù trừ khi ghi DB thất bại. Không ném lỗi nếu xoá không được. */
    public void xoaNguoiDungNeuCo(UUID userId) {
        if (userId == null) {
            return;
        }
        try {
            http.delete()
                    .uri("/admin/realms/{realm}/users/{id}", props.realm(), userId)
                    .headers(h -> h.setBearerAuth(layTokenQuanTri()))
                    .retrieve()
                    .toBodilessEntity();
        } catch (RuntimeException e) {
            log.error("Không xoá được người dùng Keycloak {} khi bù trừ: {}", userId, e.getMessage());
        }
    }

    private String layTokenQuanTri() {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "client_credentials");
        form.add("client_id", props.clientId());
        form.add("client_secret", props.clientSecret());
        JsonNode res = http.post()
                .uri("/realms/{realm}/protocol/openid-connect/token", props.realm())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .body(JsonNode.class);
        return res.path("access_token").asText();
    }

    private UUID taoNguoiDung(String token, String email, String hoTen, String matKhau, UUID tenantId) {
        Map<String, Object> body = Map.of(
                "username", email,
                "email", email,
                "firstName", hoTen,
                "enabled", true,
                "emailVerified", true, // môi trường dev chưa có SMTP; production nên xác minh email
                "attributes", Map.of("tenant_id", List.of(tenantId.toString())),
                "credentials", List.of(Map.of("type", "password", "value", matKhau, "temporary", false)));

        ResponseEntity<Void> res = http.post()
                .uri("/admin/realms/{realm}/users", props.realm())
                .headers(h -> h.setBearerAuth(token))
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .onStatus(status -> status.value() == 409, (request, response) -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Email này đã được đăng ký");
                })
                .toBodilessEntity();

        URI location = res.getHeaders().getLocation(); // .../users/{id}
        if (location == null) {
            throw new IllegalStateException("Keycloak không trả về Location của người dùng mới");
        }
        String path = location.getPath();
        return UUID.fromString(path.substring(path.lastIndexOf('/') + 1));
    }

    private void ganVaiTro(String token, UUID userId, String tenVaiTro) {
        JsonNode role = http.get()
                .uri("/admin/realms/{realm}/roles/{name}", props.realm(), tenVaiTro)
                .headers(h -> h.setBearerAuth(token))
                .retrieve()
                .body(JsonNode.class);
        http.post()
                .uri("/admin/realms/{realm}/users/{id}/role-mappings/realm", props.realm(), userId)
                .headers(h -> h.setBearerAuth(token))
                .contentType(MediaType.APPLICATION_JSON)
                .body(List.of(role))
                .retrieve()
                .toBodilessEntity();
    }
}
