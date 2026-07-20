package cc.openstrata.platform.infrastructure.adapter;

import cc.openstrata.platform.domain.port.DeploymentPort;
import java.util.List;
import java.util.Map;
import org.springframework.web.client.RestClient;

/**
 * Production {@link DeploymentPort} backed by ai-provisioning-engine's HTTP API
 * (SPECS §7.1). Translates the platform-api port signature onto the engine's
 * AssemblyPlan-based apply/rollback/status surface.
 */
public class HttpDeploymentAdapter implements DeploymentPort {

    private final RestClient client;

    public HttpDeploymentAdapter(RestClient.Builder builder) {
        this.client = builder.build();
    }

    @Override
    public String deploy(String tenantId, String agentId, String version, String specYaml) {
        Map<String, Object> plan = Map.of(
                "added", List.of(Map.of(
                        "repo_name", agentId,
                        "kind", "app",
                        "version", version)));
        Map<String, Object> body = Map.of(
                "plan", plan,
                "profile", "prod",
                "tenant_id", tenantId);
        client.post().uri("/v1/apply").body(body).retrieve().toBodilessEntity();
        return agentId + ":" + version;
    }

    @Override
    public void rollback(String agentId, String version) {
        Map<String, Object> body = Map.of(
                "component", agentId,
                "to_revision", version);
        client.post().uri("/v1/rollback").body(body).retrieve().toBodilessEntity();
    }

    @Override
    @SuppressWarnings("unchecked")
    public String getDeployStatus(String agentId, String version) {
        Map<String, Object> resp = client.get()
                .uri("/v1/status/" + agentId)
                .retrieve()
                .body(Map.class);
        if (resp == null) {
            return "UNKNOWN";
        }
        return "ready=" + resp.getOrDefault("ready", "false")
                + " version=" + resp.getOrDefault("version", "n/a");
    }
}
