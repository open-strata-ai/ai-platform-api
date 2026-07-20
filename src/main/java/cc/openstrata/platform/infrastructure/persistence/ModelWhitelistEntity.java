package cc.openstrata.platform.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.util.Objects;

/** JPA mirror of model_whitelist — platform-api-owned per-tenant model whitelist. */
@Entity
@Table(name = "model_whitelist")
@IdClass(ModelWhitelistEntity.ModelWhitelistId.class)
public class ModelWhitelistEntity {

    @Id
    @Column(name = "tenant_id")
    private String tenantId;

    @Id
    @Column(name = "model")
    private String model;

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public static class ModelWhitelistId implements Serializable {
        private String tenantId;
        private String model;

        public ModelWhitelistId() {}
        public ModelWhitelistId(String tenantId, String model) {
            this.tenantId = tenantId;
            this.model = model;
        }

        public String getTenantId() { return tenantId; }
        public void setTenantId(String tenantId) { this.tenantId = tenantId; }
        public String getModel() { return model; }
        public void setModel(String model) { this.model = model; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ModelWhitelistId that)) return false;
            return Objects.equals(tenantId, that.tenantId) && Objects.equals(model, that.model);
        }

        @Override
        public int hashCode() {
            return Objects.hash(tenantId, model);
        }
    }
}
