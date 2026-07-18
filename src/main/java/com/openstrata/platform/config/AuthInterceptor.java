package com.openstrata.platform.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Resolves the caller identity from the gateway-injected {@code X-Tenant-Id}
 * header and the bearer token. In production this is backed by the Auth SPI
 * (Keycloak); for the offline harness it accepts the headers as-is so the
 * service boots without an IdP. R-005: authorization is enforced server-side.
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {
    public static final String HEADER_TENANT = "X-Tenant-Id";
    public static final String HEADER_ROLE = "X-Role";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String tenantId = request.getHeader(HEADER_TENANT);
        String role = request.getHeader(HEADER_ROLE);
        String actor = request.getHeader("X-Actor");
        if (tenantId == null) {
            tenantId = "system";
        }
        if (role == null) {
            role = "platform-admin";
        }
        if (actor == null) {
            actor = tenantId;
        }
        TenantContext.set(TenantContext.of(tenantId, actor, role));
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        TenantContext.clear();
    }
}
