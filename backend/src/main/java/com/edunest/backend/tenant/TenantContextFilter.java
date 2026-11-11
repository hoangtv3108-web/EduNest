package com.edunest.backend.tenant;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Chạy ngay sau khi Spring Security đã xác thực JWT: lấy claim "tenant_id" đưa vào TenantContext.
 * Không phải @Component: được đăng ký thủ công trong SecurityConfig để chỉ chạy một lần, đúng vị trí.
 */
public class TenantContextFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth instanceof JwtAuthenticationToken jwtAuth) {
                String claim = jwtAuth.getToken().getClaimAsString("tenant_id");
                UUID tenantId = parse(claim);
                if (tenantId == null) {
                    // Token hợp lệ nhưng không thuộc trung tâm nào: từ chối, không để chạy tiếp.
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "Token không có tenant_id hợp lệ");
                    return;
                }
                TenantContext.set(tenantId);
            }
            chain.doFilter(request, response);
        } finally {
            TenantContext.clear(); // bắt buộc: luồng được tái sử dụng cho request khác
        }
    }

    private static UUID parse(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
