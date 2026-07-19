package cc.openstrata.platform.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.util.Objects;

/** JPA mirror of agent_skill_bindings — platform-api-owned agent↔skill bindings. */
@Entity
@Table(name = "agent_skill_bindings")
@IdClass(AgentSkillBindingEntity.AgentSkillBindingId.class)
public class AgentSkillBindingEntity {

    @Id
    @Column(name = "agent_id")
    private String agentId;

    @Id
    @Column(name = "skill_id")
    private String skillId;

    public String getAgentId() { return agentId; }
    public void setAgentId(String agentId) { this.agentId = agentId; }
    public String getSkillId() { return skillId; }
    public void setSkillId(String skillId) { this.skillId = skillId; }

    public static class AgentSkillBindingId implements Serializable {
        private String agentId;
        private String skillId;

        public AgentSkillBindingId() {}
        public AgentSkillBindingId(String agentId, String skillId) {
            this.agentId = agentId;
            this.skillId = skillId;
        }

        public String getAgentId() { return agentId; }
        public void setAgentId(String agentId) { this.agentId = agentId; }
        public String getSkillId() { return skillId; }
        public void setSkillId(String skillId) { this.skillId = skillId; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof AgentSkillBindingId that)) return false;
            return Objects.equals(agentId, that.agentId) && Objects.equals(skillId, that.skillId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(agentId, skillId);
        }
    }
}
