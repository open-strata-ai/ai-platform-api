package cc.openstrata.platform.infrastructure.adapter;

import cc.openstrata.platform.domain.port.SrsPort;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** In-memory SrsPort stand-in for Batch F2 tests (DV-06, TA-08). */
public class InMemorySrsAdapter implements SrsPort {
    private final Map<String, List<String>> byAgent = new ConcurrentHashMap<>();
    private final List<String> skills = new ArrayList<>();

    public void registerSkill(String id) {
        skills.add(id);
    }

    @Override
    public List<String> listSkills(String tenantId) {
        return new ArrayList<>(skills);
    }

    @Override
    public void bindSkill(String agentId, String skillId) {
        byAgent.computeIfAbsent(agentId, k -> new ArrayList<>()).add(skillId);
    }

    @Override
    public List<String> boundSkills(String agentId) {
        return new ArrayList<>(byAgent.getOrDefault(agentId, List.of()));
    }
}
