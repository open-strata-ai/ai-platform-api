package cc.openstrata.platform.application;

import cc.openstrata.platform.domain.port.SrsPort;
import java.util.List;
import org.springframework.stereotype.Service;

/** Use case: bind Skills/Rules/Specs to an Agent (DV-06, F2). */
@Service
public class SrsAppService {
    private final SrsPort srs;

    public SrsAppService(SrsPort srs) {
        this.srs = srs;
    }

    public List<String> listSkills() {
        return srs.listSkills("");
    }

    public void bindSkill(String agentId, String skillId) {
        srs.bindSkill(agentId, skillId);
    }

    public List<String> boundSkills(String agentId) {
        return srs.boundSkills(agentId);
    }
}
