package cc.openstrata.platform.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** JPA mirror of the applications table (V1__init.sql). */
@Entity
@Table(name = "applications")
public class ApplicationEntity {

    @Id
    @Column(name = "app_id")
    private String appId;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @Column(nullable = false)
    private String name;

    @Column(name = "agentspec_ref")
    private String agentSpecRef;

    public String getAppId() { return appId; }
    public void setAppId(String appId) { this.appId = appId; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getAgentSpecRef() { return agentSpecRef; }
    public void setAgentSpecRef(String agentSpecRef) { this.agentSpecRef = agentSpecRef; }
}
