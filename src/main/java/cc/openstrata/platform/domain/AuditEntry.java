package cc.openstrata.platform.domain;

import java.time.Instant;

/** Immutable audit-log entry (append-only, RULE-11). */
public class AuditEntry {
    private final String tenantId;
    private final String actor;
    private final String action;
    private final String payload;
    private final Instant createdAt;

    public AuditEntry(String tenantId, String actor, String action, String payload, Instant createdAt) {
        this.tenantId = tenantId;
        this.actor = actor;
        this.action = action;
        this.payload = payload;
        this.createdAt = createdAt;
    }

    public String getTenantId() {
        return tenantId;
    }

    public String getActor() {
        return actor;
    }

    public String getAction() {
        return action;
    }

    public String getPayload() {
        return payload;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
