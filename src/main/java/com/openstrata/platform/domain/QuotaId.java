package com.openstrata.platform.domain;

public record QuotaId(String value) {
    public QuotaId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("quotaId is required");
        }
    }
}
