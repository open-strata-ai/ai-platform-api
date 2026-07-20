package cc.openstrata.platform.infrastructure.persistence;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuotaJpaRepository extends JpaRepository<QuotaEntity, String> {
    List<QuotaEntity> findByTenantId(String tenantId);
    void deleteByTenantId(String tenantId);
}
