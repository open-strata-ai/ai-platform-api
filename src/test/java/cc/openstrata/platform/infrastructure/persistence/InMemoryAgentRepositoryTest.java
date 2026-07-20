package cc.openstrata.platform.infrastructure.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cc.openstrata.platform.domain.Agent;
import cc.openstrata.platform.domain.AgentVersion;
import cc.openstrata.platform.domain.port.AgentRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class InMemoryAgentRepositoryTest {
    private final AgentRepository repo = new InMemoryAgentRepository();

    @Test
    void saveAndFindById() {
        Agent a = new Agent("a1", "t1", "dev", "helper");
        repo.save(a);
        Optional<Agent> found = repo.findById("a1");
        assertTrue(found.isPresent());
        assertEquals("helper", found.get().getName());
    }

    @Test
    void findByTenantFilters() {
        repo.save(new Agent("a1", "t1", "dev", "h1"));
        repo.save(new Agent("a2", "t2", "dev", "h2"));
        assertEquals(1, repo.findByTenant("t1").size());
    }

    @Test
    void saveVersionAndList() {
        repo.save(new Agent("a1", "t1", "dev", "h"));
        AgentVersion v = new AgentVersion("a1:v1", "a1", "v1", "spec");
        repo.saveVersion(v);
        assertEquals(1, repo.findVersions("a1").size());
        repo.updateVersionStatus("a1:v1", AgentVersion.STATUS_DEPLOYED);
        assertEquals(AgentVersion.STATUS_DEPLOYED, repo.findVersion("a1", "v1").orElseThrow().getStatus());
    }
}
