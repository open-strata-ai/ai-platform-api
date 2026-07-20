package cc.openstrata.platform.infrastructure.persistence;

import cc.openstrata.platform.domain.Agent;
import cc.openstrata.platform.domain.AgentVersion;
import cc.openstrata.platform.domain.port.AgentRepository;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.transaction.annotation.Transactional;

/** JPA-backed AgentRepository. Activated by @Profile("prod"). */
public class JpaAgentRepository implements AgentRepository {

    private final AgentJpaRepository agentRepo;
    private final AgentVersionJpaRepository versionRepo;

    public JpaAgentRepository(AgentJpaRepository agentRepo, AgentVersionJpaRepository versionRepo) {
        this.agentRepo = agentRepo;
        this.versionRepo = versionRepo;
    }

    @Override
    @Transactional
    public void save(Agent agent) {
        AgentEntity e = new AgentEntity();
        e.setAgentId(agent.getAgentId());
        e.setTenantId(agent.getTenantId());
        e.setCreatedBy(agent.getCreatedBy());
        e.setName(agent.getName());
        e.setStatus(agent.getStatus());
        e.setSpec(agent.getSpec());
        agentRepo.save(e);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Agent> findById(String agentId) {
        return agentRepo.findById(agentId).map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Agent> findByTenant(String tenantId) {
        return agentRepo.findByTenantId(tenantId).stream()
                .map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void saveVersion(AgentVersion version) {
        AgentVersionEntity e = new AgentVersionEntity();
        e.setVersionId(version.getVersionId());
        e.setAgentId(version.getAgentId());
        e.setVersion(version.getVersion());
        e.setStatus(version.getStatus());
        e.setSpecSnapshot(version.getSpecSnapshot());
        e.setCreatedAt(version.getCreatedAt());
        versionRepo.save(e);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AgentVersion> findVersions(String agentId) {
        return versionRepo.findByAgentId(agentId).stream()
                .map(this::toVersionDomain).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AgentVersion> findVersion(String agentId, String version) {
        return versionRepo.findByAgentIdAndVersion(agentId, version).map(this::toVersionDomain);
    }

    @Override
    @Transactional
    public void updateVersionStatus(String versionId, String status) {
        versionRepo.findById(versionId).ifPresent(e -> {
            e.setStatus(status);
            versionRepo.save(e);
        });
    }

    private Agent toDomain(AgentEntity e) {
        Agent a = new Agent(e.getAgentId(), e.getTenantId(), e.getCreatedBy(), e.getName());
        a.setStatus(e.getStatus());
        a.setSpec(e.getSpec());
        return a;
    }

    private AgentVersion toVersionDomain(AgentVersionEntity e) {
        AgentVersion v = new AgentVersion(e.getVersionId(), e.getAgentId(), e.getVersion(),
                e.getSpecSnapshot());
        v.setStatus(e.getStatus());
        return v;
    }
}
