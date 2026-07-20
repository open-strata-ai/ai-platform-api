package cc.openstrata.platform.infrastructure.persistence;

import cc.openstrata.platform.domain.Agent;
import cc.openstrata.platform.domain.AgentVersion;
import cc.openstrata.platform.domain.port.AgentRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/** In-memory AgentRepository used as the offline stand-in for JPA (Batch C tests). */
public class InMemoryAgentRepository implements AgentRepository {
    private final Map<String, Agent> agents = new ConcurrentHashMap<>();
    private final Map<String, List<AgentVersion>> versions = new ConcurrentHashMap<>();

    @Override
    public void save(Agent agent) {
        agents.put(agent.getAgentId(), agent);
    }

    @Override
    public Optional<Agent> findById(String agentId) {
        return Optional.ofNullable(agents.get(agentId));
    }

    @Override
    public List<Agent> findByTenant(String tenantId) {
        List<Agent> out = new ArrayList<>();
        for (Agent a : agents.values()) {
            if (a.getTenantId().equals(tenantId)) {
                out.add(a);
            }
        }
        return out;
    }

    @Override
    public void saveVersion(AgentVersion version) {
        versions.computeIfAbsent(version.getAgentId(), k -> new ArrayList<>()).add(version);
    }

    @Override
    public List<AgentVersion> findVersions(String agentId) {
        return new ArrayList<>(versions.getOrDefault(agentId, List.of()));
    }

    @Override
    public Optional<AgentVersion> findVersion(String agentId, String version) {
        return findVersions(agentId).stream()
                .filter(v -> v.getVersion().equals(version))
                .findFirst();
    }

    @Override
    public void updateVersionStatus(String versionId, String status) {
        for (List<AgentVersion> vs : versions.values()) {
            for (AgentVersion v : vs) {
                if (v.getVersionId().equals(versionId)) {
                    v.setStatus(status);
                    return;
                }
            }
        }
    }
}
