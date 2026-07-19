package cc.openstrata.platform.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** JPA mirror of the quotas table (V1__init.sql). */
@Entity
@Table(name = "quotas")
public class QuotaEntity {

    @Id
    @Column(name = "quota_id")
    private String quotaId;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @Column(nullable = false)
    private String dimension;

    @Column(name = "limit_val", nullable = false)
    private long limitVal;

    @Column(name = "used_val", nullable = false)
    private long usedVal;

    public String getQuotaId() { return quotaId; }
    public void setQuotaId(String quotaId) { this.quotaId = quotaId; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getDimension() { return dimension; }
    public void setDimension(String dimension) { this.dimension = dimension; }
    public long getLimitVal() { return limitVal; }
    public void setLimitVal(long limitVal) { this.limitVal = limitVal; }
    public long getUsedVal() { return usedVal; }
    public void setUsedVal(long usedVal) { this.usedVal = usedVal; }
}
