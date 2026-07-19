package cc.openstrata.platform.domain;

/**
 * Agent aggregate root (DV-01..05, DV-11, DV-15). The AgentSpec is NOT a typed
 * entity; per DESIGN §4.3.5 it is a YAML runtime contract serialized as a
 * String stored on this aggregate and on each AgentVersion (reuse AgentSpecRef
 * for the apiVersion/kind/name reference). Status: DRAFT|PUBLISHED|DEPRECATED.
 */
public class Agent {
    public static final String STATUS_DRAFT = "DRAFT";
    public static final String STATUS_PUBLISHED = "PUBLISHED";
    public static final String STATUS_DEPRECATED = "DEPRECATED";

    private final String agentId;
    private final String tenantId;
    private final String createdBy;
    private String name;
    private String status;
    private String spec; // AgentSpec YAML

    public Agent(String agentId, String tenantId, String createdBy, String name) {
        this.agentId = agentId;
        this.tenantId = tenantId;
        this.createdBy = createdBy;
        this.name = name;
        this.status = STATUS_DRAFT;
    }

    public String getAgentId() {
        return agentId;
    }

    public String getTenantId() {
        return tenantId;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSpec() {
        return spec;
    }

    public void setSpec(String spec) {
        this.spec = spec;
    }

    public void publish() {
        this.status = STATUS_PUBLISHED;
    }

    public void deprecate() {
        this.status = STATUS_DEPRECATED;
    }
}
