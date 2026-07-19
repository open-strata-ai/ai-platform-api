package cc.openstrata.platform.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.io.Serializable;

/** JPA mirror of the model_grants table (composite PK: tenant_id, provider, model). */
@Entity
@Table(name = "model_grants")
@IdClass(ModelGrantEntity.ModelGrantId.class)
public class ModelGrantEntity {

    @Id
    @Column(name = "tenant_id")
    private String tenantId;

    @Id
    private String provider;

    @Id
    private String model;

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public static class ModelGrantId implements Serializable {
        private String tenantId;
        private String provider;
        private String model;

        public ModelGrantId() {}
        public ModelGrantId(String tenantId, String provider, String model) {
            this.tenantId = tenantId;
            this.provider = provider;
            this.model = model;
        }

        public String getTenantId() { return tenantId; }
        public String getProvider() { return provider; }
        public String getModel() { return model; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ModelGrantId that)) return false;
            return java.util.Objects.equals(tenantId, that.tenantId)
                && java.util.Objects.equals(provider, that.provider)
                && java.util.Objects.equals(model, that.model);
        }

        @Override
        public int hashCode() {
            return java.util.Objects.hash(tenantId, provider, model);
        }
    }
}
