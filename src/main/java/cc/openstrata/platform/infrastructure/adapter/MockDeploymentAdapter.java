package cc.openstrata.platform.infrastructure.adapter;

import cc.openstrata.platform.domain.AgentVersion;
import cc.openstrata.platform.domain.port.DeploymentPort;

/**
 * MockDeploymentAdapter satisfies {@link DeploymentPort} in Batch C so the
 * publish→deploy flow is fully testable without ai-provisioning-engine (Batch J,
 * F5). It marks the AgentVersion status = DEPLOYED and records the deployment;
 * no K8s/Compose interaction occurs. The real gRPC adapter replaces this in
 * Batch J (E2E-23).
 */
public class MockDeploymentAdapter implements DeploymentPort {
    private final java.util.Map<String, String> statuses = new java.util.concurrent.ConcurrentHashMap<>();

    @Override
    public String deploy(String tenantId, String agentId, String version, String specYaml) {
        String key = agentId + ":" + version;
        statuses.put(key, AgentVersion.STATUS_DEPLOYED);
        return key;
    }

    @Override
    public void rollback(String agentId, String version) {
        String key = agentId + ":" + version;
        statuses.put(key, AgentVersion.STATUS_ROLLED_BACK);
    }

    @Override
    public String getDeployStatus(String agentId, String version) {
        return statuses.getOrDefault(agentId + ":" + version, "UNKNOWN");
    }
}
