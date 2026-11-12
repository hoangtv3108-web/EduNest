package com.edunest.backend.tenant;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * TC02: backend chỉ tin mã trung tâm trong token đã ký, bỏ qua mọi mã do client
 * gửi lên.
 */
@SpringBootTest
@AutoConfigureMockMvc
class TenantContextHttpTest {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private JdbcTemplate jdbc;
    @Autowired
    private TransactionTemplate tx;

    private UUID tenantA;
    private UUID tenantB;

    @BeforeEach
    void setUp() {
        tenantA = UUID.randomUUID();
        tenantB = UUID.randomUUID();
        taoTrungTam(tenantA, "Trung tâm A");
        taoTrungTam(tenantB, "Trung tâm B");
    }

    @AfterEach
    void tearDown() {
        xoaTrungTam(tenantA);
        xoaTrungTam(tenantB);
    }

    @Test
    @DisplayName("TC02: người dùng A gửi mã trung tâm của B qua header và tham số => vẫn chỉ nhận trung tâm A")
    void maTrungTamTuClientBiBoQua() throws Exception {
        mvc.perform(get("/api/tenant/current")
                .param("tenant_id", tenantB.toString())
                .param("tenantId", tenantB.toString())
                .header("X-Tenant-Id", tenantB.toString())
                .with(jwt().jwt(j -> j.claim("tenant_id", tenantA.toString()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(tenantA.toString()))
                .andExpect(jsonPath("$.tenTrungTam").value("Trung tâm A"));
    }

    @Test
    @DisplayName("Token hợp lệ nhưng không có tenant_id => 403")
    void tokenKhongCoTenantBiTuChoi() throws Exception {
        mvc.perform(get("/api/tenant/current").with(jwt()))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Token mang tenant_id chưa tồn tại => không thấy dữ liệu của trung tâm nào (404)")
    void tenantKhongTonTaiKhongThayGi() throws Exception {
        mvc.perform(get("/api/tenant/current")
                .with(jwt().jwt(j -> j.claim("tenant_id", UUID.randomUUID().toString()))))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Không có token => 401")
    void khongCoTokenBiTuChoi() throws Exception {
        mvc.perform(get("/api/tenant/current")).andExpect(status().isUnauthorized());
    }

    private void taoTrungTam(UUID id, String ten) {
        try {
            TenantContext.set(id);
            tx.executeWithoutResult(s -> jdbc.update("INSERT INTO tenant (id, ten_trung_tam) VALUES (?, ?)", id, ten));
        } finally {
            TenantContext.clear();
        }
    }

    private void xoaTrungTam(UUID id) {
        try {
            TenantContext.set(id);
            tx.executeWithoutResult(s -> {
                jdbc.update("DELETE FROM tai_khoan WHERE tenant_id = ?", id);
                jdbc.update("DELETE FROM tenant WHERE id = ?", id);
            });
        } finally {
            TenantContext.clear();
        }
    }
}