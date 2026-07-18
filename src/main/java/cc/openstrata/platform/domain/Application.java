package cc.openstrata.platform.domain;

/** Application (Agent App) entity within the Tenant aggregate. */
public class Application {
    private final AppId appId;
    private final TenantId tenantId;
    private String name;
    private AgentSpecRef agentSpecRef;

    public Application(AppId appId, TenantId tenantId, String name, AgentSpecRef agentSpecRef) {
        this.appId = appId;
        this.tenantId = tenantId;
        this.name = name;
        this.agentSpecRef = agentSpecRef;
    }

    public AppId getAppId() {
        return appId;
    }

    public TenantId getTenantId() {
        return tenantId;
    }

    public String getName() {
        return name;
    }

    public AgentSpecRef getAgentSpecRef() {
        return agentSpecRef;
    }
}
