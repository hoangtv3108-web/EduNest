package com.edunest.backend.tenant;

import com.edunest.backend.keycloak.KeycloakAdminClient;
import java.util.Locale;
import java.util.UUID;
import org.springframework.stereotype.Service;

/**
 * Luồng đăng ký trung tâm:
 *  1. Sinh tenantId.
 *  2. Tạo người dùng quản trị trên Keycloak (gắn thuộc tính tenant_id + vai trò CENTER_ADMIN).
 *  3. Ghi tenant và tai_khoan vào PostgreSQL (qua RLS).
 *  4. Nếu bước 3 lỗi thì xoá người dùng vừa tạo ở Keycloak để không để lại tài khoản mồ côi.
 */
@Service
public class TenantRegistrationService {

    private final KeycloakAdminClient keycloak;
    private final TenantRepository repository;

    public TenantRegistrationService(KeycloakAdminClient keycloak, TenantRepository repository) {
        this.keycloak = keycloak;
        this.repository = repository;
    }

    public RegisterTenantResponse dangKy(RegisterTenantRequest req) {
        UUID tenantId = UUID.randomUUID();
        String email = req.emailQuanTri().trim().toLowerCase(Locale.ROOT);
        String hoTen = req.hoTenQuanTri().trim();
        String tenTrungTam = req.tenTrungTam().trim();

        UUID keycloakUserId = keycloak.taoQuanTriTrungTam(email, hoTen, req.matKhau(), tenantId);
        try {
            TenantContext.set(tenantId);
            repository.taoTrungTamVaQuanTri(tenantId, tenTrungTam, keycloakUserId, email, hoTen);
        } catch (RuntimeException e) {
            keycloak.xoaNguoiDungNeuCo(keycloakUserId);
            throw e;
        } finally {
            TenantContext.clear();
        }
        return new RegisterTenantResponse(tenantId, tenTrungTam, email);
    }
}
