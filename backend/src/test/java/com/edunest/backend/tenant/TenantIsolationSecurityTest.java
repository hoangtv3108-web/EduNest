package com.edunest.backend.tenant;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class TenantIsolationSecurityTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private TransactionTemplate transactionTemplate;

    private UUID tenantAId;
    private UUID tenantBId;

    @BeforeEach
    void setUp() {
        tenantAId = UUID.randomUUID();
        tenantBId = UUID.randomUUID();

        // Chuẩn bị dữ liệu cho Tenant A
        TenantContext.set(tenantAId);
        transactionTemplate.execute(status -> {
            jdbcTemplate.update("INSERT INTO tenant (id, ten_trung_tam) VALUES (?, ?)", tenantAId, "Trung tâm A");
            jdbcTemplate.update(
                    "INSERT INTO tai_khoan (tenant_id, keycloak_sub, email, ho_ten, vai_tro) VALUES (?, ?, ?, ?, ?)",
                    tenantAId, UUID.randomUUID(), "adminA@edunest.vn", "Admin Tenant A", "CENTER_ADMIN");
            return null;
        });

        // Chuẩn bị dữ liệu cho Tenant B
        TenantContext.set(tenantBId);
        transactionTemplate.execute(status -> {
            jdbcTemplate.update("INSERT INTO tenant (id, ten_trung_tam) VALUES (?, ?)", tenantBId, "Trung tâm B");
            jdbcTemplate.update(
                    "INSERT INTO tai_khoan (tenant_id, keycloak_sub, email, ho_ten, vai_tro) VALUES (?, ?, ?, ?, ?)",
                    tenantBId, UUID.randomUUID(), "adminB@edunest.vn", "Admin Tenant B", "CENTER_ADMIN");
            return null;
        });

        TenantContext.clear();
    }

    @AfterEach
    void tearDown() {
        // Dọn dẹp dữ liệu kiểm thử
        TenantContext.set(tenantAId);
        transactionTemplate.execute(status -> {
            jdbcTemplate.update("DELETE FROM tai_khoan WHERE tenant_id = ?", tenantAId);
            jdbcTemplate.update("DELETE FROM tenant WHERE id = ?", tenantAId);
            return null;
        });

        TenantContext.set(tenantBId);
        transactionTemplate.execute(status -> {
            jdbcTemplate.update("DELETE FROM tai_khoan WHERE tenant_id = ?", tenantBId);
            jdbcTemplate.update("DELETE FROM tenant WHERE id = ?", tenantBId);
            return null;
        });

        TenantContext.clear();
    }

    @Test
    @DisplayName("Kịch bản 1: User Tenant A query không có WHERE tenant_id chỉ đọc được dữ liệu của chính mình")
    void testTenantCannotSeeOtherTenantData() {
        TenantContext.set(tenantAId);

        transactionTemplate.execute(status -> {
            // Cố tình SELECT ALL không có mệnh đề WHERE tenant_id
            List<String> emails = jdbcTemplate.queryForList("SELECT email FROM tai_khoan", String.class);

            assertThat(emails)
                    .contains("adminA@edunest.vn")
                    .doesNotContain("adminB@edunest.vn");
            return null;
        });
    }

    @Test
    @DisplayName("Kịch bản 2: User Tenant A cố ý INSERT bản ghi với tenant_id của Tenant B (Bị RLS WITH CHECK chặn)")
    void testTenantCannotInsertIntoOtherTenant() {
        TenantContext.set(tenantAId);

        transactionTemplate.execute(status -> {
            assertThatThrownBy(() -> {
                jdbcTemplate.update(
                        "INSERT INTO tai_khoan (tenant_id, keycloak_sub, email, ho_ten, vai_tro) VALUES (?, ?, ?, ?, ?)",
                        tenantBId, UUID.randomUUID(), "hacker@edunest.vn", "Hacker", "CENTER_ADMIN");
            }).isInstanceOf(DataAccessException.class)
                    .hasStackTraceContaining("row-level security policy"); // kiểm tra cả lỗi gốc

            status.setRollbackOnly(); // transaction đã lỗi nên hủy hẳn, không commit
            return null;
        });
    }

    @Test
    @DisplayName("Kịch bản 3: User Tenant A cố ý UPDATE hoặc DELETE bản ghi của Tenant B (Không ảnh hưởng - 0 hàng)")
    void testTenantCannotModifyOtherTenantData() {
        TenantContext.set(tenantAId);

        transactionTemplate.execute(status -> {
            int rowsUpdated = jdbcTemplate.update(
                    "UPDATE tai_khoan SET ho_ten = 'Bị tấn công' WHERE email = 'adminB@edunest.vn'");
            assertThat(rowsUpdated).isZero();

            int rowsDeleted = jdbcTemplate.update(
                    "DELETE FROM tai_khoan WHERE email = 'adminB@edunest.vn'");
            assertThat(rowsDeleted).isZero();

            return null;
        });
    }

    @Test
    @DisplayName("Kịch bản 4: Request không có tenant_id (Fail-Closed) => PostgreSQL trả về 0 dòng")
    void testFailClosedWhenNoTenantContext() {
        TenantContext.clear();

        transactionTemplate.execute(status -> {
            List<String> danhSach = jdbcTemplate.queryForList("SELECT email FROM tai_khoan", String.class);
            assertThat(danhSach).isEmpty();
            return null;
        });
    }
}
