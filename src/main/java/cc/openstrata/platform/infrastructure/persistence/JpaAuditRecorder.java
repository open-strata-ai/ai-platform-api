package cc.openstrata.platform.infrastructure.persistence;

import cc.openstrata.platform.domain.port.AuditRecorder;
import java.time.Instant;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/** JPA-backed AuditRecorder. Activated by @Profile("prod"). */
@Repository
public class JpaAuditRecorder implements AuditRecorder {

    private final AuditJpaRepository repo;

    public JpaAuditRecorder(AuditJpaRepository repo) {
        this.repo = repo;
    }

    @Override
    @Transactional
    public void record(String tenantId, String actor, String action, String payload) {
        AuditEntity e = new AuditEntity();
        e.setTenantId(tenantId);
        e.setActor(actor);
        e.setAction(action);
        e.setPayload(payload);
        e.setCreatedAt(Instant.now());
        repo.save(e);
    }
}
