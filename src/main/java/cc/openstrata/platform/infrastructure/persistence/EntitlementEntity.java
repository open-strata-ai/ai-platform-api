package cc.openstrata.platform.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.io.Serializable;

/** JPA mirror of the entitlements table (composite PK: tenant_id, component). */
@Entity
@Table(name = "entitlements")
@IdClass(EntitlementEntity.EntitlementId.class)
public class EntitlementEntity {

    @Id
    @Column(name = "tenant_id")
    private String tenantId;

    @Id
    private String component;

    @Column(nullable = false)
    private boolean allowed;

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getComponent() { return component; }
    public void setComponent(String component) { this.component = component; }
    public boolean isAllowed() { return allowed; }
    public void setAllowed(boolean allowed) { this.allowed = allowed; }

    public static class EntitlementId implements Serializable {
        private String tenantId;
        private String component;

        public EntitlementId() {}
        public EntitlementId(String tenantId, String component) {
            this.tenantId = tenantId;
            this.component = component;
        }

        public String getTenantId() { return tenantId; }
        public String getComponent() { return component; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof EntitlementId that)) return false;
            return java.util.Objects.equals(tenantId, that.tenantId)
                && java.util.Objects.equals(component, that.component);
        }

        @Override
        public int hashCode() {
            return java.util.Objects.hash(tenantId, component);
        }
    }
}
