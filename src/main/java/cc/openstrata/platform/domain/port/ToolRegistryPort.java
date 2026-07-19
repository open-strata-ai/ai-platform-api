package cc.openstrata.platform.domain.port;

import java.util.List;

/** Read-only view of registered tools in ai-tool-registry (DV-03 / DV-14). */
public interface ToolRegistryPort {
    List<ToolSpec> listTools(String tenantId);

    void bindTool(String agentId, String toolId);

    /** Lightweight tool descriptor projected from the registry. */
    class ToolSpec {
        public final String id;
        public final String name;

        public ToolSpec(String id, String name) {
            this.id = id;
            this.name = name;
        }
    }
}
