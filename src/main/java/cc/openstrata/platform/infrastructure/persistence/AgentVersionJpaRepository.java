package cc.openstrata.platform.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AgentVersionJpaRepository extends JpaRepository<AgentVersionEntity, String> {
    List<AgentVersionEntity> findByAgentId(String agentId);

    Optional<AgentVersionEntity> findByAgentIdAndVersion(String agentId, String version);
}
