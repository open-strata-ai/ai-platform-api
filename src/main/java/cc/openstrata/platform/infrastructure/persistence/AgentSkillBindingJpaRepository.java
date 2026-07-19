package cc.openstrata.platform.infrastructure.persistence;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AgentSkillBindingJpaRepository
        extends JpaRepository<AgentSkillBindingEntity, AgentSkillBindingEntity.AgentSkillBindingId> {

    List<AgentSkillBindingEntity> findByAgentId(String agentId);

    void deleteByAgentId(String agentId);
}
