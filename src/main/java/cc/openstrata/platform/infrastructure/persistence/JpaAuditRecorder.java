package cc.openstrata.platform.infrastructure.persistence;

import cc.openstrata.platform.domain.port.AuditRecorder;
import java.time.Instant;
import org.springframework.transaction.annotation.Transactional;

/**
 * JPA-backed AuditRecorder. Must NOT carry a stereotype annotation: it is
 * instantiated only via the @Bean factory in PlatformApiProductionConfig, which
 * is gated by @Profile("prod"). A @Repository here would let component scanning
 * instantiate it in every profile and fail to start (no AuditJpaRepository in
 * non-prod). Mirrors the existing JpaTenantRepository / JpaPlanRepository pattern.
 */
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
