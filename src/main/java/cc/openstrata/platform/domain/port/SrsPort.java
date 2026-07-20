package cc.openstrata.platform.domain.port;

import java.util.List;

/** Skills/Rules/Specs (SRS) SPI (DV-06 skill binding, TA-08 security rules). */
public interface SrsPort {
    List<String> listSkills(String tenantId);

    void bindSkill(String agentId, String skillId);

    List<String> boundSkills(String agentId);
}
