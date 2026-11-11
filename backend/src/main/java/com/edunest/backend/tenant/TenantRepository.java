package com.edunest.backend.tenant;

import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Truy cập bảng có RLS. Phải gọi từ bên ngoài bean này (qua proxy) để @Transactional có tác dụng,
 * và TenantContext phải được đặt trước khi gọi.
 */
@Repository
public class TenantRepository {

    private final JdbcClient jdbc;

    public TenantRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * Tạo trung tâm và tài khoản quản trị đầu tiên trong cùng một transaction.
     * Qua được chính sách RLS (WITH CHECK) vì TenantContext = tenantId đang được tạo.
     */
    @Transactional
    public void taoTrungTamVaQuanTri(
            UUID tenantId, String tenTrungTam, UUID keycloakSub, String email, String hoTen) {
        jdbc.sql("INSERT INTO tenant (id, ten_trung_tam) VALUES (:id, :ten)")
                .param("id", tenantId)
                .param("ten", tenTrungTam)
                .update();
        jdbc.sql("""
                INSERT INTO tai_khoan (tenant_id, keycloak_sub, email, ho_ten, vai_tro)
                VALUES (:tenantId, :sub, :email, :hoTen, 'CENTER_ADMIN')
                """)
                .param("tenantId", tenantId)
                .param("sub", keycloakSub)
                .param("email", email)
                .param("hoTen", hoTen)
                .update();
    }

    /** Không có WHERE: nhờ RLS chỉ còn đúng 1 hàng là trung tâm của người đang gọi. */
    @Transactional(readOnly = true)
    public Optional<TenantInfo> timTrungTamHienTai() {
        return jdbc.sql("SELECT id, ten_trung_tam, trang_thai, ngay_tao FROM tenant")
                .query((rs, rowNum) -> new TenantInfo(
                        rs.getObject("id", UUID.class),
                        rs.getString("ten_trung_tam"),
                        rs.getString("trang_thai"),
                        rs.getTimestamp("ngay_tao").toLocalDateTime()))
                .optional();
    }
}
