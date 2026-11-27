package com.edunest.backend.security;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

@SpringBootTest
@AutoConfigureMockMvc
class AccessControlTest {

    @Autowired
    MockMvc mvc;

    static final String TENANT = "11111111-1111-1111-1111-111111111111";

    private static RequestPostProcessor as(String role) {
        return jwt().jwt(j -> j.claim("tenant_id", TENANT))
                .authorities(new SimpleGrantedAuthority("ROLE_" + role));
    }

    @Test
    void khongCoToken_401() throws Exception {
        mvc.perform(get("/api/lop")).andExpect(status().isUnauthorized());
    }

    @Test
    void tokenKhongCoTenantId_403() throws Exception {
        mvc.perform(get("/api/lop").with(jwt()
                .authorities(new SimpleGrantedAuthority("ROLE_CENTER_ADMIN"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void giaoVien_khongDuocXacNhanDaNopHocPhi_403() throws Exception {
        mvc.perform(patch("/api/hoa-don/{id}/trang-thai", UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON).content("{}").with(as("TEACHER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void giaoVien_khongDuocXoaLop_403() throws Exception {
        mvc.perform(delete("/api/lop/{id}", UUID.randomUUID()).with(as("TEACHER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void giaoVien_khongDuocImportExcel_403() throws Exception {
        var f = new MockMultipartFile("file", "x.xlsx", "application/octet-stream", new byte[] { 1 });
        mvc.perform(multipart("/api/hoc-vien/excel/import").file(f).with(as("TEACHER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void phuHuynh_khongVaoDuocApiQuanTri_403() throws Exception {
        mvc.perform(get("/api/lop").with(as("PARENT"))).andExpect(status().isForbidden());
    }

    @Test
    void giaoVien_vanXemDuocDanhSachLop() throws Exception {
        int s = mvc.perform(get("/api/lop").with(as("TEACHER"))).andReturn().getResponse().getStatus();
        assertNotEquals(403, s);
    }
}