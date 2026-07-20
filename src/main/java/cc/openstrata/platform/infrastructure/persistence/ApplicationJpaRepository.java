package cc.openstrata.platform.infrastructure.persistence;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApplicationJpaRepository extends JpaRepository<ApplicationEntity, String> {
    List<ApplicationEntity> findByTenantId(String tenantId);
    void deleteByTenantId(String tenantId);
}
