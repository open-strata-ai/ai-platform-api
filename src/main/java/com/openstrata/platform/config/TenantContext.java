package com.openstrata.platform.config;

/**
 * Per-request security context (R-005: server-side RBAC happens here). Populated
 * by {@link AuthInterceptor} from the gateway-injected {@code X-Tenant-Id} header
 * and the bearer token claims. The holder is a ThreadLocal so application code
 * can read the current caller without threading it through every method.
 */
public final class TenantContext {
    private final String tenantId;
    private final String actor;
    private final String role;

    private TenantContext(String tenantId, String actor, String role) {
        this.tenantId = tenantId;
        this.actor = actor;
        this.role = role;
    }

    public static TenantContext of(String tenantId, String actor, String role) {
        return new TenantContext(tenantId, actor, role);
    }

    public String getTenantId() {
        return tenantId;
    }

    public String getActor() {
        return actor;
    }

    public String getRole() {
        return role;
    }

    public boolean isPlatformAdmin() {
        return "platform-admin".equals(role);
    }

    // --- ThreadLocal holder ---
    private static final ThreadLocal<TenantContext> HOLDER = new ThreadLocal<>();

    public static void set(TenantContext ctx) {
        HOLDER.set(ctx);
    }

    public static TenantContext get() {
        return HOLDER.get();
    }

    public static void clear() {
        HOLDER.remove();
    }
}
