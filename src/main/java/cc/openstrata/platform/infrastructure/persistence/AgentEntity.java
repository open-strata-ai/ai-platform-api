package cc.openstrata.platform.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** JPA mirror of the agents table (V4__agents.sql). spec is AgentSpec YAML (TEXT). */
@Entity
@Table(name = "agents")
public class AgentEntity {

    @Id
    @Column(name = "agent_id")
    private String agentId;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @Column(name = "created_by", nullable = false)
    private String createdBy;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String status;

    @Column(name = "spec", columnDefinition = "TEXT")
    private String spec;

    public String getAgentId() { return agentId; }
    public void setAgentId(String agentId) { this.agentId = agentId; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getSpec() { return spec; }
    public void setSpec(String spec) { this.spec = spec; }
}
