package cc.openstrata.platform.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.util.Objects;

/** JPA mirror of eval_runs — platform-api-owned record of eval runs per agent. */
@Entity
@Table(name = "eval_runs")
@IdClass(EvalRunEntity.EvalRunId.class)
public class EvalRunEntity {

    @Id
    @Column(name = "agent_id")
    private String agentId;

    @Id
    @Column(name = "run_id")
    private String runId;

    public String getAgentId() { return agentId; }
    public void setAgentId(String agentId) { this.agentId = agentId; }
    public String getRunId() { return runId; }
    public void setRunId(String runId) { this.runId = runId; }

    public static class EvalRunId implements Serializable {
        private String agentId;
        private String runId;

        public EvalRunId() {}
        public EvalRunId(String agentId, String runId) {
            this.agentId = agentId;
            this.runId = runId;
        }

        public String getAgentId() { return agentId; }
        public void setAgentId(String agentId) { this.agentId = agentId; }
        public String getRunId() { return runId; }
        public void setRunId(String runId) { this.runId = runId; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof EvalRunId that)) return false;
            return Objects.equals(agentId, that.agentId) && Objects.equals(runId, that.runId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(agentId, runId);
        }
    }
}
