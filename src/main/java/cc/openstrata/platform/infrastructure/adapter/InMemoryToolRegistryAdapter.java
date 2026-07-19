package cc.openstrata.platform.infrastructure.adapter;

import cc.openstrata.platform.domain.port.ToolRegistryPort;
import java.util.ArrayList;
import java.util.List;

/** In-memory ToolRegistryPort stand-in for Batch C tests (DV-03). */
public class InMemoryToolRegistryAdapter implements ToolRegistryPort {
    private final List<ToolSpec> tools = new ArrayList<>();
    private final List<String> bound = new ArrayList<>();

    public void register(String id, String name) {
        tools.add(new ToolSpec(id, name));
    }

    @Override
    public List<ToolSpec> listTools(String tenantId) {
        return new ArrayList<>(tools);
    }

    @Override
    public void bindTool(String agentId, String toolId) {
        bound.add(agentId + ":" + toolId);
    }

    public boolean isBound(String agentId, String toolId) {
        return bound.contains(agentId + ":" + toolId);
    }
}
