package cc.openstrata.platform.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cc.openstrata.platform.application.dto.PublishVersionRequest;
import cc.openstrata.platform.application.dto.VersionResponse;
import cc.openstrata.platform.domain.Agent;
import cc.openstrata.platform.domain.AgentVersion;
import cc.openstrata.platform.domain.AgentVersionService;
import cc.openstrata.platform.domain.port.AgentRepository;
import cc.openstrata.platform.infrastructure.adapter.MockDeploymentAdapter;
import cc.openstrata.platform.infrastructure.persistence.InMemoryAgentRepository;
import org.junit.jupiter.api.Test;

class AgentPublishingAppServiceTest {
    private final AgentRepository repo = new InMemoryAgentRepository();
    private final MockDeploymentAdapter deploy = new MockDeploymentAdapter();
    private final AgentPublishingAppService svc =
            new AgentPublishingAppService(repo, new AgentVersionService(repo), deploy);

    private Agent agent() {
        Agent a = new Agent("a1", "t1", "dev", "helper");
        a.setSpec("apiVersion: agent/v1\nkind: Agent\n");
        repo.save(a);
        return a;
    }

    @Test
    void publishMarksVersionDeployedAndAgentPublished() {
        agent();
        VersionResponse v = svc.publishVersion("a1", new PublishVersionRequest("v1", "first"));
        assertEquals(AgentVersion.STATUS_DEPLOYED, v.status());
        assertEquals("DEPLOYED", deploy.getDeployStatus("a1", "v1"));
        assertEquals("PUBLISHED", repo.findById("a1").orElseThrow().getStatus());
    }

    @Test
    void rollbackMarksVersionRolledBack() {
        agent();
        svc.publishVersion("a1", new PublishVersionRequest("v1", "first"));
        VersionResponse v = svc.rollbackToVersion("a1", "v1");
        assertEquals(AgentVersion.STATUS_ROLLED_BACK, v.status());
        assertEquals("ROLLED_BACK", deploy.getDeployStatus("a1", "v1"));
    }

    @Test
    void listVersionsReturnsPublished() {
        agent();
        svc.publishVersion("a1", new PublishVersionRequest("v1", "first"));
        assertTrue(svc.listVersions("a1").size() == 1);
    }
}
