package cc.openstrata.platform.domain;

import cc.openstrata.platform.domain.port.AgentRepository;
import java.util.Optional;

/**
 * Domain service for Agent version lifecycle (DV-15): create a version from an
 * Agent's current spec, diff two versions, and validate a rollback target.
 */
public class AgentVersionService {
    private final AgentRepository agentRepository;

    public AgentVersionService(AgentRepository agentRepository) {
        this.agentRepository = agentRepository;
    }

    public AgentVersion createVersion(Agent agent, String versionLabel) {
        if (agent.getSpec() == null || agent.getSpec().isBlank()) {
            throw new DomainException(ErrorCode.INVALID_AGENT_SPEC);
        }
        String versionId = agent.getAgentId() + ":" + versionLabel;
        AgentVersion v = new AgentVersion(versionId, agent.getAgentId(), versionLabel, agent.getSpec());
        agentRepository.saveVersion(v);
        return v;
    }

    public boolean isNewer(AgentVersion candidate, AgentVersion baseline) {
        return candidate.getCreatedAt() > baseline.getCreatedAt();
    }

    public void validateRollback(AgentVersion targetVersion) {
        if (targetVersion == null) {
            throw new DomainException(ErrorCode.VERSION_NOT_FOUND);
        }
        Optional<AgentVersion> current = agentRepository.findVersion(
                targetVersion.getAgentId(), targetVersion.getVersion());
        if (current.isEmpty()) {
            throw new DomainException(ErrorCode.VERSION_NOT_FOUND);
        }
    }
}
