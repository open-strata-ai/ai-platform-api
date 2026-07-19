package cc.openstrata.platform.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.util.Objects;

/** JPA mirror of agent_tool_bindings — platform-api-owned agent↔tool bindings. */
@Entity
@Table(name = "agent_tool_bindings")
@IdClass(AgentToolBindingEntity.AgentToolBindingId.class)
public class AgentToolBindingEntity {

    @Id
    @Column(name = "agent_id")
    private String agentId;

    @Id
    @Column(name = "tool_id")
    private String toolId;

    public String getAgentId() { return agentId; }
    public void setAgentId(String agentId) { this.agentId = agentId; }
    public String getToolId() { return toolId; }
    public void setToolId(String toolId) { this.toolId = toolId; }

    public static class AgentToolBindingId implements Serializable {
        private String agentId;
        private String toolId;

        public AgentToolBindingId() {}
        public AgentToolBindingId(String agentId, String toolId) {
            this.agentId = agentId;
            this.toolId = toolId;
        }

        public String getAgentId() { return agentId; }
        public void setAgentId(String agentId) { this.agentId = agentId; }
        public String getToolId() { return toolId; }
        public void setToolId(String toolId) { this.toolId = toolId; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof AgentToolBindingId that)) return false;
            return Objects.equals(agentId, that.agentId) && Objects.equals(toolId, that.toolId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(agentId, toolId);
        }
    }
}
