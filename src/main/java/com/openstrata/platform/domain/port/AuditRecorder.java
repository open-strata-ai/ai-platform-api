package com.openstrata.platform.domain.port;

/** Immutable audit_log recorder (append-only). */
public interface AuditRecorder {
    void record(String tenantId, String actor, String action, String payload);
}
