package com.openstrata.platform.infrastructure.adapter;

import com.openstrata.platform.domain.AuditEntry;
import com.openstrata.platform.domain.port.AuditRecorder;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/** Offline AuditRecorder — append-only in-memory list (immutable audit_log). */
public class InMemoryAuditRecorder implements AuditRecorder {
    private final List<AuditEntry> entries = new ArrayList<>();

    @Override
    public void record(String tenantId, String actor, String action, String payload) {
        entries.add(new AuditEntry(tenantId, actor, action, payload, Instant.now()));
    }

    public List<AuditEntry> entries() {
        return List.copyOf(entries);
    }
}
