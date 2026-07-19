package cc.openstrata.platform.infrastructure.persistence;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EntitlementJpaRepository
    extends JpaRepository<EntitlementEntity, EntitlementEntity.EntitlementId> {
    List<EntitlementEntity> findByTenantId(String tenantId);
    void deleteByTenantId(String tenantId);
}
