package cc.openstrata.platform.infrastructure.persistence;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ModelGrantJpaRepository
    extends JpaRepository<ModelGrantEntity, ModelGrantEntity.ModelGrantId> {
    List<ModelGrantEntity> findByTenantId(String tenantId);
    void deleteByTenantId(String tenantId);
}
