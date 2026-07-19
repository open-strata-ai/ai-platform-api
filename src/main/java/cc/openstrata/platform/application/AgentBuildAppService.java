package cc.openstrata.platform.application;

import cc.openstrata.platform.application.dto.AgentResponse;
import cc.openstrata.platform.application.dto.BindToolRequest;
import cc.openstrata.platform.application.dto.ConfigureMemoryRequest;
import cc.openstrata.platform.application.dto.ConfigureModelRequest;
import cc.openstrata.platform.application.dto.CreateAgentRequest;
import cc.openstrata.platform.domain.Agent;
import cc.openstrata.platform.domain.AgentSpecBuilderService;
import cc.openstrata.platform.domain.AgentSpecBuilderService.MemoryConfig;
import cc.openstrata.platform.domain.AgentSpecBuilderService.ModelBinding;
import cc.openstrata.platform.domain.AgentSpecBuilderService.ToolBinding;
import cc.openstrata.platform.domain.DomainException;
import cc.openstrata.platform.domain.ErrorCode;
import cc.openstrata.platform.config.TenantContext;
import cc.openstrata.platform.domain.port.AgentRepository;
import cc.openstrata.platform.domain.port.ModelRegistryPort;
import cc.openstrata.platform.domain.port.ToolRegistryPort;
import java.util.List;
import java.util.UUID;

/** Agent build use cases (DV-01..05). */
public class AgentBuildAppService {
    private final AgentRepository agentRepository;
    private final AgentSpecBuilderService specBuilder;
    private final ToolRegistryPort toolRegistry;
    private final ModelRegistryPort modelRegistry;

    public AgentBuildAppService(AgentRepository agentRepository, AgentSpecBuilderService specBuilder,
                                ToolRegistryPort toolRegistry, ModelRegistryPort modelRegistry) {
        this.agentRepository = agentRepository;
        this.specBuilder = specBuilder;
        this.toolRegistry = toolRegistry;
        this.modelRegistry = modelRegistry;
    }

    private String tenant() {
        TenantContext ctx = TenantContext.get();
        return ctx != null ? ctx.getTenantId() : "local";
    }

    private String actor() {
        TenantContext ctx = TenantContext.get();
        return ctx != null ? ctx.getActor() : "system";
    }

    public AgentResponse createAgent(CreateAgentRequest req) {
        String id = UUID.randomUUID().toString();
        Agent agent = new Agent(id, tenant(), actor(), req.name());
        String spec = specBuilder.assembleSpec(new AgentSpecBuilderService.AgentBuildCommand(
                req.name(), req.model(), req.memoryEnabled(), List.of()));
        agent.setSpec(spec);
        agentRepository.save(agent);
        return toResponse(agent);
    }

    public AgentResponse bindTool(String agentId, BindToolRequest req) {
        Agent agent = load(agentId);
        String spec = specBuilder.mergeToolBindings(agent.getSpec(), List.of(new ToolBinding(req.toolId())));
        agent.setSpec(spec);
        toolRegistry.bindTool(agentId, req.toolId());
        agentRepository.save(agent);
        return toResponse(agent);
    }

    public AgentResponse configureMemory(String agentId, ConfigureMemoryRequest req) {
        Agent agent = load(agentId);
        String spec = specBuilder.mergeMemoryConfig(agent.getSpec(),
                new MemoryConfig(req.enabled(), req.maxTokens()));
        agent.setSpec(spec);
        agentRepository.save(agent);
        return toResponse(agent);
    }

    public AgentResponse configureModel(String agentId, ConfigureModelRequest req) {
        Agent agent = load(agentId);
        String spec = specBuilder.mergeModelBinding(agent.getSpec(),
                new ModelBinding(req.model(), req.fallbackChain()));
        agent.setSpec(spec);
        modelRegistry.assignWhitelist(tenant(), List.of(req.model()));
        agentRepository.save(agent);
        return toResponse(agent);
    }

    private Agent load(String agentId) {
        return agentRepository.findById(agentId)
                .orElseThrow(() -> new DomainException(ErrorCode.AGENT_NOT_FOUND));
    }

    public AgentResponse getAgent(String agentId) {
        return toResponse(load(agentId));
    }

    private AgentResponse toResponse(Agent a) {
        return new AgentResponse(a.getAgentId(), a.getTenantId(), a.getName(), a.getStatus(), a.getSpec());
    }
}
