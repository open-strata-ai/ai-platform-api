package cc.openstrata.platform.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class AgentSpecBuilderServiceTest {
    private final AgentSpecBuilderService svc = new AgentSpecBuilderService();

    @Test
    void assembleSpecIncludesNameModelAndTools() {
        String yaml = svc.assembleSpec(new AgentSpecBuilderService.AgentBuildCommand(
                "helper", "qwen-max", true, List.of("web_search")));
        assertTrue(yaml.contains("name: helper"));
        assertTrue(yaml.contains("model: qwen-max"));
        assertTrue(yaml.contains("enabled: true"));
        assertTrue(yaml.contains("- web_search"));
    }

    @Test
    void mergeToolBindingsAppendsTool() {
        String base = svc.assembleSpec(new AgentSpecBuilderService.AgentBuildCommand("a", "m", false, List.of()));
        String merged = svc.mergeToolBindings(base, List.of(new AgentSpecBuilderService.ToolBinding("calc")));
        assertTrue(merged.contains("- calc"));
    }

    @Test
    void mergeModelBindingUpdatesModelAndAddsFallback() {
        String base = svc.assembleSpec(new AgentSpecBuilderService.AgentBuildCommand("a", "m", false, List.of()));
        String merged = svc.mergeModelBinding(base, new AgentSpecBuilderService.ModelBinding("gpt-4o", List.of("m")));
        assertTrue(merged.contains("model: gpt-4o"));
        assertTrue(merged.contains("fallback_chain: m"));
    }

    @Test
    void mergeMemoryConfigTogglesEnabled() {
        String base = svc.assembleSpec(new AgentSpecBuilderService.AgentBuildCommand("a", "m", false, List.of()));
        String merged = svc.mergeMemoryConfig(base, new AgentSpecBuilderService.MemoryConfig(true, 4096));
        assertTrue(merged.contains("enabled: true"));
        assertFalse(merged.contains("enabled: false"));
    }
}
