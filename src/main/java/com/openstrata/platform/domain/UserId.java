package com.openstrata.platform.domain;

public record UserId(String value) {
    public UserId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("userId is required");
        }
    }
}
