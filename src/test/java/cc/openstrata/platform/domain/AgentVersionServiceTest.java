package cc.openstrata.platform.domain;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cc.openstrata.platform.domain.port.AgentRepository;
import cc.openstrata.platform.infrastructure.persistence.InMemoryAgentRepository;
import org.junit.jupiter.api.Test;

class AgentVersionServiceTest {
    private final AgentRepository repo = new InMemoryAgentRepository();
    private final AgentVersionService svc = new AgentVersionService(repo);

    private Agent agent() {
        Agent a = new Agent("a1", "t1", "dev", "helper");
        a.setSpec("apiVersion: agent/v1\nkind: Agent\n");
        repo.save(a);
        return a;
    }

    @Test
    void createVersionSnapshotsSpec() {
        AgentVersion v = svc.createVersion(agent(), "v1");
        assertTrue(v.getSpecSnapshot().contains("kind: Agent"));
        assertTrue(repo.findVersions("a1").size() == 1);
    }

    @Test
    void validateRollbackRejectsMissing() {
        svc.createVersion(agent(), "v1");
        AgentVersion missing = new AgentVersion("x", "a1", "v9", "spec");
        assertThrows(DomainException.class, () -> svc.validateRollback(missing));
    }
}
