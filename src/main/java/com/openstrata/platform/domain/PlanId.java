package com.openstrata.platform.domain;

public record PlanId(String value) {
    public PlanId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("planId is required");
        }
    }
}
