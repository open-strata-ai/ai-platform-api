package com.openstrata.platform.domain;

/**
 * Per-plan quota template: upper limits for each resource dimension. GPU is only
 * applicable when the tenant enables self-hosted inference (full profile).
 */
public record QuotaTemplate(long cpu, long token, long qps, long vector, long gpu) {
    public QuotaTemplate {
        if (gpu < 0 || cpu < 0 || token < 0 || qps < 0 || vector < 0) {
            throw new IllegalArgumentException("quota values must be >= 0");
        }
    }

    public QuotaTemplate disableGpu() {
        return new QuotaTemplate(cpu, token, qps, vector, 0L);
    }

    public boolean isGpuEnabled() {
        return gpu > 0;
    }
}
