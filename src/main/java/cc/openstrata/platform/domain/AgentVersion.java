package cc.openstrata.platform.domain;

/**
 * A versioned snapshot of an Agent (DV-15). Status: DRAFT|DEPLOYED|ROLLED_BACK.
 * The spec_snapshot is the AgentSpec YAML frozen at publish time.
 */
public class AgentVersion {
    public static final String STATUS_DRAFT = "DRAFT";
    public static final String STATUS_DEPLOYED = "DEPLOYED";
    public static final String STATUS_ROLLED_BACK = "ROLLED_BACK";

    private final String versionId;
    private final String agentId;
    private final String version; // semver
    private String status;
    private String specSnapshot; // AgentSpec YAML
    private long createdAt;

    public AgentVersion(String versionId, String agentId, String version, String specSnapshot) {
        this.versionId = versionId;
        this.agentId = agentId;
        this.version = version;
        this.specSnapshot = specSnapshot;
        this.status = STATUS_DRAFT;
        this.createdAt = System.currentTimeMillis();
    }

    public String getVersionId() {
        return versionId;
    }

    public String getAgentId() {
        return agentId;
    }

    public String getVersion() {
        return version;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSpecSnapshot() {
        return specSnapshot;
    }

    public void setSpecSnapshot(String specSnapshot) {
        this.specSnapshot = specSnapshot;
    }

    public long getCreatedAt() {
        return createdAt;
    }
}
