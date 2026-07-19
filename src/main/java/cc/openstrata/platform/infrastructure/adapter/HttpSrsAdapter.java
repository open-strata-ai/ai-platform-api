package cc.openstrata.platform.infrastructure.adapter;

import cc.openstrata.platform.domain.port.SrsPort;
import cc.openstrata.platform.infrastructure.persistence.AgentSkillBindingEntity;
import cc.openstrata.platform.infrastructure.persistence.AgentSkillBindingJpaRepository;
import java.util.ArrayList;
import java.util.List;
import org.springframework.web.client.RestClient;

/**
 * Production {@link SrsPort}: lists skills from ai-srs-service over HTTP and
 * persists agent↔skill bindings in platform-api's own DB.
 */
public class HttpSrsAdapter implements SrsPort {

    private final RestClient client;
    private final AgentSkillBindingJpaRepository bindingRepo;

    public HttpSrsAdapter(RestClient.Builder builder,
                          AgentSkillBindingJpaRepository bindingRepo) {
        this.client = builder.build();
        this.bindingRepo = bindingRepo;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<String> listSkills(String tenantId) {
        List<String> names = client.get()
                .uri(u -> u.path("/api/v1/skills").queryParam("tenant", tenantId).build())
                .header("X-Tenant-Id", tenantId)
                .header("Authorization", "Bearer " + tenantId + ":admin")
                .retrieve()
                .body(List.class);
        return names == null ? new ArrayList<>() : names;
    }

    @Override
    public void bindSkill(String agentId, String skillId) {
        AgentSkillBindingEntity e = new AgentSkillBindingEntity();
        e.setAgentId(agentId);
        e.setSkillId(skillId);
        bindingRepo.save(e);
    }

    @Override
    public List<String> boundSkills(String agentId) {
        return bindingRepo.findByAgentId(agentId).stream()
                .map(AgentSkillBindingEntity::getSkillId)
                .toList();
    }
}
