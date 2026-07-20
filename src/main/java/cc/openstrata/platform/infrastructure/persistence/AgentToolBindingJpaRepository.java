package cc.openstrata.platform.infrastructure.persistence;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AgentToolBindingJpaRepository
        extends JpaRepository<AgentToolBindingEntity, AgentToolBindingEntity.AgentToolBindingId> {

    List<AgentToolBindingEntity> findByAgentId(String agentId);

    void deleteByAgentId(String agentId);
}
