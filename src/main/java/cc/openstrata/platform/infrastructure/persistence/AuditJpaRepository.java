package cc.openstrata.platform.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditJpaRepository extends JpaRepository<AuditEntity, Long> {
}
