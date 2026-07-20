package cc.openstrata.platform.infrastructure.persistence;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AgentJpaRepository extends JpaRepository<AgentEntity, String> {
    List<AgentEntity> findByTenantId(String tenantId);
}
