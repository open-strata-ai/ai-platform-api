package com.openstrata.platform.domain;

/** Quota entity (per resource dimension) within the Tenant aggregate. */
public class Quota {
    private final QuotaId quotaId;
    private final TenantId tenantId;
    private final ResourceDimension dimension;
    private long limit;
    private long used;

    public Quota(QuotaId quotaId, TenantId tenantId, ResourceDimension dimension, long limit) {
        this.quotaId = quotaId;
        this.tenantId = tenantId;
        this.dimension = dimension;
        this.limit = limit;
        this.used = 0;
    }

    public void setLimit(long limit) {
        if (limit < 0) {
            throw new IllegalArgumentException("limit must be >= 0");
        }
        this.limit = limit;
    }

    public QuotaId getQuotaId() {
        return quotaId;
    }

    public TenantId getTenantId() {
        return tenantId;
    }

    public ResourceDimension getDimension() {
        return dimension;
    }

    public long getLimit() {
        return limit;
    }

    public long getUsed() {
        return used;
    }
}
