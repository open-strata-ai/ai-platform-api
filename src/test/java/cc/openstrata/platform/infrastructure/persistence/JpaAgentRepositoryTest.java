package cc.openstrata.platform.infrastructure.persistence;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import cc.openstrata.platform.domain.Agent;
import cc.openstrata.platform.domain.AgentVersion;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JpaAgentRepositoryTest {

    private AgentJpaRepository agentJpa;
    private AgentVersionJpaRepository versionJpa;
    private JpaAgentRepository repo;

    @BeforeEach
    void setUp() {
        agentJpa = mock(AgentJpaRepository.class);
        versionJpa = mock(AgentVersionJpaRepository.class);
        repo = new JpaAgentRepository(agentJpa, versionJpa);
    }

    @Test
    void savePersistsAgentFields() {
        Agent a = new Agent("a1", "t1", "u1", "My Agent");
        a.setSpec("apiVersion: v1\nkind: Agent");
        a.publish();

        repo.save(a);

        verify(agentJpa).save(argThat(e ->
            "a1".equals(e.getAgentId())
                && "t1".equals(e.getTenantId())
                && "u1".equals(e.getCreatedBy())
                && "My Agent".equals(e.getName())
                && Agent.STATUS_PUBLISHED.equals(e.getStatus())
                && e.getSpec().contains("kind: Agent")));
    }

    @Test
    void findByIdReconstructsAgent() {
        AgentEntity e = new AgentEntity();
        e.setAgentId("a1");
        e.setTenantId("t1");
        e.setCreatedBy("u1");
        e.setName("My Agent");
        e.setStatus(Agent.STATUS_PUBLISHED);
        e.setSpec("spec-yaml");
        when(agentJpa.findById("a1")).thenReturn(Optional.of(e));

        Optional<Agent> result = repo.findById("a1");
        assertTrue(result.isPresent());
        assertEquals("My Agent", result.get().getName());
        assertEquals("t1", result.get().getTenantId());
        assertEquals(Agent.STATUS_PUBLISHED, result.get().getStatus());
        assertEquals("spec-yaml", result.get().getSpec());
    }

    @Test
    void findByTenantMapsRows() {
        AgentEntity e = new AgentEntity();
        e.setAgentId("a1");
        e.setTenantId("t1");
        e.setCreatedBy("u1");
        e.setName("A1");
        e.setStatus(Agent.STATUS_DRAFT);
        when(agentJpa.findByTenantId("t1")).thenReturn(List.of(e));

        List<Agent> all = repo.findByTenant("t1");
        assertEquals(1, all.size());
        assertEquals("a1", all.get(0).getAgentId());
    }

    @Test
    void saveVersionPersistsFields() {
        AgentVersion v = new AgentVersion("v1", "a1", "1.0.0", "snapshot-yaml");

        repo.saveVersion(v);

        verify(versionJpa).save(argThat(e ->
            "v1".equals(e.getVersionId())
                && "a1".equals(e.getAgentId())
                && "1.0.0".equals(e.getVersion())
                && AgentVersion.STATUS_DRAFT.equals(e.getStatus())
                && "snapshot-yaml".equals(e.getSpecSnapshot())
                && e.getCreatedAt() > 0));
    }

    @Test
    void findVersionsMapsRows() {
        AgentVersionEntity e = new AgentVersionEntity();
        e.setVersionId("v1");
        e.setAgentId("a1");
        e.setVersion("1.0.0");
        e.setStatus(AgentVersion.STATUS_DEPLOYED);
        e.setSpecSnapshot("snap");
        e.setCreatedAt(123L);
        when(versionJpa.findByAgentId("a1")).thenReturn(List.of(e));

        List<AgentVersion> versions = repo.findVersions("a1");
        assertEquals(1, versions.size());
        assertEquals("1.0.0", versions.get(0).getVersion());
        assertEquals(AgentVersion.STATUS_DEPLOYED, versions.get(0).getStatus());
    }

    @Test
    void findVersionDelegatesToQuery() {
        AgentVersionEntity e = new AgentVersionEntity();
        e.setVersionId("v1");
        e.setAgentId("a1");
        e.setVersion("1.0.0");
        e.setStatus(AgentVersion.STATUS_DRAFT);
        when(versionJpa.findByAgentIdAndVersion("a1", "1.0.0")).thenReturn(Optional.of(e));

        Optional<AgentVersion> result = repo.findVersion("a1", "1.0.0");
        assertTrue(result.isPresent());
        assertEquals("v1", result.get().getVersionId());
    }

    @Test
    void updateVersionStatusPersistsChange() {
        AgentVersionEntity e = new AgentVersionEntity();
        e.setVersionId("v1");
        e.setAgentId("a1");
        e.setVersion("1.0.0");
        e.setStatus(AgentVersion.STATUS_DRAFT);
        when(versionJpa.findById("v1")).thenReturn(Optional.of(e));

        repo.updateVersionStatus("v1", AgentVersion.STATUS_DEPLOYED);

        verify(versionJpa).save(argThat(saved ->
            AgentVersion.STATUS_DEPLOYED.equals(saved.getStatus())));
    }

    @Test
    void updateVersionStatusNoOpWhenMissing() {
        when(versionJpa.findById("missing")).thenReturn(Optional.empty());
        repo.updateVersionStatus("missing", AgentVersion.STATUS_DEPLOYED);
        verify(versionJpa, never()).save(any());
    }
}
