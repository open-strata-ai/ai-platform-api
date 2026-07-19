package cc.openstrata.platform.infrastructure.persistence;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ModelWhitelistJpaRepository
        extends JpaRepository<ModelWhitelistEntity, ModelWhitelistEntity.ModelWhitelistId> {

    List<ModelWhitelistEntity> findByTenantId(String tenantId);

    void deleteByTenantId(String tenantId);
}
