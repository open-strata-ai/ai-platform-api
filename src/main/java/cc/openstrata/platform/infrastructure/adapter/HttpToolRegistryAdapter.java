package cc.openstrata.platform.infrastructure.adapter;

import cc.openstrata.platform.domain.port.ToolRegistryPort;
import cc.openstrata.platform.infrastructure.persistence.AgentToolBindingEntity;
import cc.openstrata.platform.infrastructure.persistence.AgentToolBindingJpaRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.web.client.RestClient;

/**
 * Production {@link ToolRegistryPort}: lists tools from ai-tool-registry over HTTP
 * and persists agent↔tool bindings in platform-api's own DB (binding is a
 * platform-api aggregate, not an operation the registry exposes).
 */
public class HttpToolRegistryAdapter implements ToolRegistryPort {

    private final RestClient client;
    private final AgentToolBindingJpaRepository bindingRepo;

    public HttpToolRegistryAdapter(RestClient.Builder builder,
                                   AgentToolBindingJpaRepository bindingRepo) {
        this.client = builder.build();
        this.bindingRepo = bindingRepo;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<ToolSpec> listTools(String tenantId) {
        Map<String, Object> resp = client.get()
                .uri(u -> u.path("/v1/tools").queryParam("tenant_id", tenantId).build())
                .header("X-Tenant-Id", tenantId)
                .header("Authorization", "Bearer " + tenantId + ":admin")
                .retrieve()
                .body(Map.class);
        List<Map<String, Object>> tools = resp == null ? null
                : (List<Map<String, Object>>) resp.get("tools");
        List<ToolSpec> out = new ArrayList<>();
        if (tools != null) {
            for (Map<String, Object> t : tools) {
                String name = String.valueOf(t.get("name"));
                out.add(new ToolSpec(name, name));
            }
        }
        return out;
    }

    @Override
    public void bindTool(String agentId, String toolId) {
        AgentToolBindingEntity e = new AgentToolBindingEntity();
        e.setAgentId(agentId);
        e.setToolId(toolId);
        bindingRepo.save(e);
    }
}
