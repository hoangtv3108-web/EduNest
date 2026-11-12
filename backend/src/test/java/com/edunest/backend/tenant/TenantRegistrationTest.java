package com.edunest.backend.tenant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.edunest.backend.keycloak.KeycloakAdminClient;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.server.ResponseStatusException;

/**
 * TC03: đăng ký trung tâm. Keycloak được giả lập nên chạy được trong CI mà
 * không cần Keycloak thật.
 */
@SpringBootTest
@AutoConfigureMockMvc
class TenantRegistrationTest {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private TenantRegistrationService service;
    @Autowired
    private JdbcTemplate jdbc;
    @Autowired
    private TransactionTemplate tx;
    @MockitoBean
    private KeycloakAdminClient keycloak;

    private final List<UUID> canDon = new ArrayList<>();

    @AfterEach
    void donDep() {
        for (UUID id : canDon) {
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

    private static final String JSON_HOP_LE = """
            {"tenTrungTam":"  Trung tâm Anh ngữ Demo ","emailQuanTri":"Admin@Demo.VN",
             "hoTenQuanTri":"Nguyễn Văn A","matKhau":"matkhau123"}""";

    @Test
    @DisplayName("TC03: đăng ký tạo tenant + tài khoản quản trị; trung tâm khác không thấy dữ liệu đó")
    void dangKyThanhCong() throws Exception {
        UUID sub = UUID.randomUUID();
        when(keycloak.taoQuanTriTrungTam(anyString(), anyString(), anyString(), any(UUID.class))).thenReturn(sub);

        String body = mvc
                .perform(post("/api/public/tenants").contentType(MediaType.APPLICATION_JSON).content(JSON_HOP_LE))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tenTrungTam").value("Trung tâm Anh ngữ Demo"))
                .andExpect(jsonPath("$.emailQuanTri").value("admin@demo.vn"))
                .andReturn().getResponse().getContentAsString();

        ArgumentCaptor<UUID> tenantCaptor = ArgumentCaptor.forClass(UUID.class);
        verify(keycloak).taoQuanTriTrungTam(eq("admin@demo.vn"), eq("Nguyễn Văn A"), eq("matkhau123"),
                tenantCaptor.capture());
        UUID tenantId = tenantCaptor.getValue();
        canDon.add(tenantId);
        assertThat(body).contains(tenantId.toString());

        // Trong ngữ cảnh của chính trung tâm: thấy 1 tenant và 1 tài khoản quản trị
        // đúng vai trò.
        TenantContext.set(tenantId);
        try {
            tx.executeWithoutResult(s -> {
                assertThat(jdbc.queryForObject("SELECT count(*) FROM tenant", Integer.class)).isEqualTo(1);
                assertThat(jdbc.queryForList("SELECT vai_tro FROM tai_khoan WHERE keycloak_sub = ?", String.class,
                        sub.toString()))
                        .containsExactly("CENTER_ADMIN");
            });
        } finally {
            TenantContext.clear();
        }

        // Trong ngữ cảnh của trung tâm khác: không thấy gì.
        TenantContext.set(UUID.randomUUID());
        try {
            tx.executeWithoutResult(s -> {
                assertThat(jdbc.queryForObject("SELECT count(*) FROM tenant", Integer.class)).isZero();
                assertThat(jdbc.queryForObject("SELECT count(*) FROM tai_khoan", Integer.class)).isZero();
            });
        } finally {
            TenantContext.clear();
        }
    }

    @Test
    @DisplayName("Dữ liệu không hợp lệ => 400 và không gọi Keycloak")
    void duLieuKhongHopLe() throws Exception {
        String sai = """
                {"tenTrungTam":"","emailQuanTri":"khong-phai-email","hoTenQuanTri":"A","matKhau":"123"}""";
        mvc.perform(post("/api/public/tenants").contentType(MediaType.APPLICATION_JSON).content(sai))
                .andExpect(status().isBadRequest());
        verify(keycloak, never()).taoQuanTriTrungTam(anyString(), anyString(), anyString(), any(UUID.class));
    }

    @Test
    @DisplayName("Email đã tồn tại trên Keycloak => 409 và không để lại tenant nào")
    void emailTrung() throws Exception {
        when(keycloak.taoQuanTriTrungTam(anyString(), anyString(), anyString(), any(UUID.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.CONFLICT, "Email này đã được đăng ký"));

        mvc.perform(post("/api/public/tenants").contentType(MediaType.APPLICATION_JSON).content(JSON_HOP_LE))
                .andExpect(status().isConflict());

        ArgumentCaptor<UUID> tenantCaptor = ArgumentCaptor.forClass(UUID.class);
        verify(keycloak).taoQuanTriTrungTam(anyString(), anyString(), anyString(), tenantCaptor.capture());
        canDon.add(tenantCaptor.getValue());
        assertThat(soTenant(tenantCaptor.getValue())).isZero();
    }

    @Test
    @DisplayName("Ghi DB thất bại => xoá người dùng vừa tạo trên Keycloak (bù trừ) và không để lại tenant")
    void buTruKhiGhiDbLoi() {
        UUID subTrung = UUID.randomUUID();
        when(keycloak.taoQuanTriTrungTam(anyString(), anyString(), anyString(), any(UUID.class))).thenReturn(subTrung);
        RegisterTenantRequest req = new RegisterTenantRequest("Trung tâm 1", "a1@demo.vn", "Người A", "matkhau123");
        RegisterTenantRequest req2 = new RegisterTenantRequest("Trung tâm 2", "a2@demo.vn", "Người B", "matkhau123");

        UUID tenant1 = service.dangKy(req).tenantId();
        canDon.add(tenant1);

        // Lần 2 trả về cùng keycloak_sub => vi phạm UNIQUE(keycloak_sub) khi ghi DB.
        assertThatThrownBy(() -> service.dangKy(req2)).isInstanceOf(DataAccessException.class);
        verify(keycloak).xoaNguoiDungNeuCo(subTrung);

        ArgumentCaptor<UUID> tenantCaptor = ArgumentCaptor.forClass(UUID.class);
        verify(keycloak, org.mockito.Mockito.times(2))
                .taoQuanTriTrungTam(anyString(), anyString(), anyString(), tenantCaptor.capture());
        UUID tenant2 = tenantCaptor.getAllValues().get(1);
        canDon.add(tenant2);
        assertThat(soTenant(tenant2)).isZero(); // transaction đã rollback cả tenant lẫn tài khoản
    }

    private int soTenant(UUID tenantId) {
        TenantContext.set(tenantId);
        try {
            return tx.execute(s -> jdbc.queryForObject("SELECT count(*) FROM tenant", Integer.class));
        } finally {
            TenantContext.clear();
        }
    }
}