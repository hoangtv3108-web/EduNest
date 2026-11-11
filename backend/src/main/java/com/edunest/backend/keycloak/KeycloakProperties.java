package com.edunest.backend.keycloak;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Cấu hình lấy từ prefix "keycloak" trong application.yml. */
@ConfigurationProperties(prefix = "keycloak")
public record KeycloakProperties(String baseUrl, String realm, String clientId, String clientSecret) {}
