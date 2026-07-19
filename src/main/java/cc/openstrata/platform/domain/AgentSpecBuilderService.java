package cc.openstrata.platform.domain;

import java.util.List;

/**
 * Domain service that assembles the AgentSpec YAML runtime contract (DESIGN §4.1,
 * F2). The spec is a {@code String} (YAML), NOT a typed entity; it is stored on
 * {@link Agent} / {@link AgentVersion} and referenced by {@link AgentSpecRef}.
 */
public class AgentSpecBuilderService {

    /** Command to assemble a fresh AgentSpec. */
    public record AgentBuildCommand(String name, String model, boolean memoryEnabled, List<String> tools) {}

    /** Value types reused by the merge operations. */
    public record ToolBinding(String toolId) {}
    public record ModelBinding(String model, List<String> fallbackChain) {}
    public record MemoryConfig(boolean enabled, int maxTokens) {}

    public String assembleSpec(AgentBuildCommand cmd) {
        StringBuilder b = new StringBuilder();
        b.append("apiVersion: agent/v1\n");
        b.append("kind: Agent\n");
        b.append("metadata:\n");
        b.append("  name: ").append(cmd.name()).append("\n");
        b.append("spec:\n");
        b.append("  model: ").append(cmd.model()).append("\n");
        b.append("  memory:\n");
        b.append("    enabled: ").append(cmd.memoryEnabled()).append("\n");
        b.append("  tools:\n");
        for (String t : cmd.tools()) {
            b.append("    - ").append(t).append("\n");
        }
        return b.toString();
    }

    public String mergeToolBindings(String specYaml, List<ToolBinding> tools) {
        StringBuilder b = new StringBuilder(specYaml);
        if (!b.toString().contains("  tools:")) {
            b.append("  tools:\n");
        }
        for (ToolBinding t : tools) {
            b.append("    - ").append(t.toolId()).append("\n");
        }
        return b.toString();
    }

    public String mergeModelBinding(String specYaml, ModelBinding binding) {
        return specYaml.replaceFirst("(?m)^  model: .*$", "  model: " + binding.model())
                + "  fallback_chain: " + String.join(",", binding.fallbackChain()) + "\n";
    }

    public String mergeMemoryConfig(String specYaml, MemoryConfig config) {
        return specYaml.replaceFirst("(?m)^    enabled: .*$",
                "    enabled: " + config.enabled());
    }
}
