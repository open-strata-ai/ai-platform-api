package cc.openstrata.platform.domain.port;

import cc.openstrata.platform.domain.Agent;
import cc.openstrata.platform.domain.AgentVersion;
import java.util.List;
import java.util.Optional;

/** Persistence port for the Agent aggregate and its versions (DESIGN §5.1). */
public interface AgentRepository {
    void save(Agent agent);

    Optional<Agent> findById(String agentId);

    List<Agent> findByTenant(String tenantId);

    void saveVersion(AgentVersion version);

    List<AgentVersion> findVersions(String agentId);

    Optional<AgentVersion> findVersion(String agentId, String version);

    void updateVersionStatus(String versionId, String status);
}
