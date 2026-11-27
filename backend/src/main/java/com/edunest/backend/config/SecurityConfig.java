package com.edunest.backend.config;

import com.edunest.backend.tenant.TenantContextFilter;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // API stateless dùng Bearer token, không dùng cookie phiên
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.GET, "/api/hello").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/public/tenants").permitAll()
                        .requestMatchers("/actuator/health", "/actuator/health/**", "/error").permitAll()
                        .requestMatchers("/api/tenant/current").authenticated()
                        // Chỉ quản lý trung tâm
                        .requestMatchers("/api/audit-log/**", "/api/bao-cao/**", "/api/nhac-no/**",
                                "/api/hoa-don/**", "/api/ghi-danh/**", "/api/hoc-vien/excel/**")
                                .hasRole("CENTER_ADMIN")
                        // Giáo viên: xem lớp/học viên, điểm danh
                        .requestMatchers(HttpMethod.GET, "/api/lop/**", "/api/hoc-vien/**")
                                .hasAnyRole("CENTER_ADMIN", "TEACHER")
                        .requestMatchers("/api/diem-danh/**").hasAnyRole("CENTER_ADMIN", "TEACHER")
                        // Còn lại trong /api (tạo/sửa/xóa lớp, buổi học...): chỉ quản lý
                        .requestMatchers("/api/**").hasRole("CENTER_ADMIN")
                        // Mặc định từ chối mọi đường dẫn chưa khai báo
                        .anyRequest().denyAll())
                .oauth2ResourceServer(oauth -> oauth.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtConverter())))
                // Chạy sau khi JWT đã được xác thực: lấy tenant_id từ token đã ký.
                .addFilterAfter(new TenantContextFilter(), BearerTokenAuthenticationFilter.class);
        return http.build();
    }

    /**
     * Đổi realm role của Keycloak (claim realm_access.roles) thành
     * ROLE_CENTER_ADMIN, ROLE_TEACHER, ...
     */
    private JwtAuthenticationConverter jwtConverter() {
        Converter<Jwt, Collection<GrantedAuthority>> authorities = jwt -> {
            Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
            if (realmAccess == null || !(realmAccess.get("roles") instanceof Collection<?> roles)) {
                return List.of();
            }
            return roles.stream()
                    .map(r -> (GrantedAuthority) new SimpleGrantedAuthority("ROLE_" + r))
                    .toList();
        };
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(authorities);
        return converter;
    }
}