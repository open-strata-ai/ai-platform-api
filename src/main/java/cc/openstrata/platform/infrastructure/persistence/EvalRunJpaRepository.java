package cc.openstrata.platform.infrastructure.persistence;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EvalRunJpaRepository
        extends JpaRepository<EvalRunEntity, EvalRunEntity.EvalRunId> {

    List<EvalRunEntity> findByAgentId(String agentId);
}
