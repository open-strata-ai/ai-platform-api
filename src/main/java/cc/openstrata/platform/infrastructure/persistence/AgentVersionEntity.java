package cc.openstrata.platform.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** JPA mirror of the agent_versions table (V4__agents.sql). spec_snapshot is YAML (TEXT). */
@Entity
@Table(name = "agent_versions")
public class AgentVersionEntity {

    @Id
    @Column(name = "version_id")
    private String versionId;

    @Column(name = "agent_id", nullable = false)
    private String agentId;

    @Column(name = "version", nullable = false)
    private String version;

    @Column(nullable = false)
    private String status;

    @Column(name = "spec_snapshot", columnDefinition = "TEXT")
    private String specSnapshot;

    @Column(name = "created_at", nullable = false)
    private long createdAt;

    public String getVersionId() { return versionId; }
    public void setVersionId(String versionId) { this.versionId = versionId; }
    public String getAgentId() { return agentId; }
    public void setAgentId(String agentId) { this.agentId = agentId; }
    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getSpecSnapshot() { return specSnapshot; }
    public void setSpecSnapshot(String specSnapshot) { this.specSnapshot = specSnapshot; }
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}
