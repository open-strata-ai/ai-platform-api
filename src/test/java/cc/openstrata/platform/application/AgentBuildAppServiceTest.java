package cc.openstrata.platform.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cc.openstrata.platform.application.dto.AgentResponse;
import cc.openstrata.platform.application.dto.BindToolRequest;
import cc.openstrata.platform.application.dto.ConfigureMemoryRequest;
import cc.openstrata.platform.application.dto.ConfigureModelRequest;
import cc.openstrata.platform.application.dto.CreateAgentRequest;
import cc.openstrata.platform.config.TenantContext;
import cc.openstrata.platform.domain.AgentSpecBuilderService;
import cc.openstrata.platform.domain.port.AgentRepository;
import cc.openstrata.platform.infrastructure.adapter.InMemoryModelRegistryAdapter;
import cc.openstrata.platform.infrastructure.adapter.InMemoryToolRegistryAdapter;
import cc.openstrata.platform.infrastructure.persistence.InMemoryAgentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AgentBuildAppServiceTest {
    private final AgentRepository repo = new InMemoryAgentRepository();
    private final AgentBuildAppService svc = new AgentBuildAppService(repo,
            new AgentSpecBuilderService(), new InMemoryToolRegistryAdapter(), new InMemoryModelRegistryAdapter());

    @BeforeEach
    void ctx() {
        TenantContext.set(TenantContext.of("t1", "dev", "developer"));
    }

    @Test
    void createAgentAssemblesSpec() {
        AgentResponse r = svc.createAgent(new CreateAgentRequest("helper", "qwen-max", true));
        assertTrue(r.spec().contains("name: helper"));
        assertTrue(r.spec().contains("model: qwen-max"));
        assertEquals("DRAFT", r.status());
    }

    @Test
    void bindToolDelegatesToRegistry() {
        AgentResponse created = svc.createAgent(new CreateAgentRequest("h", "m", false));
        InMemoryToolRegistryAdapter tools = new InMemoryToolRegistryAdapter();
        AgentBuildAppService svc2 = new AgentBuildAppService(repo, new AgentSpecBuilderService(), tools,
                new InMemoryModelRegistryAdapter());
        AgentResponse r = svc2.bindTool(created.agentId(), new BindToolRequest("calc"));
        assertTrue(r.spec().contains("- calc"));
    }

    @Test
    void configureModelUpdatesWhitelist() {
        AgentResponse created = svc.createAgent(new CreateAgentRequest("h", "m", false));
        InMemoryModelRegistryAdapter models = new InMemoryModelRegistryAdapter();
        AgentBuildAppService svc2 = new AgentBuildAppService(repo, new AgentSpecBuilderService(),
                new InMemoryToolRegistryAdapter(), models);
        svc2.configureModel(created.agentId(), new ConfigureModelRequest("gpt-4o", java.util.List.of("m")));
        assertTrue(models.getWhitelist().contains("gpt-4o"));
    }

    @Test
    void configureMemoryTogglesEnabled() {
        AgentResponse created = svc.createAgent(new CreateAgentRequest("h", "m", false));
        AgentResponse r = svc.configureMemory(created.agentId(), new ConfigureMemoryRequest(true, 2048));
        assertTrue(r.spec().contains("enabled: true"));
    }
}
