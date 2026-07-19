package cc.openstrata.platform.domain.port;

/**
 * Deployment SPI to ai-provisioning-engine (Go). In Batch C this is satisfied by
 * {@code MockDeploymentAdapter}; the real gRPC adapter is wired in Batch J
 * (DESIGN §6.1, §6.2). The spec is the serialized AgentSpec YAML.
 */
public interface DeploymentPort {
    String deploy(String tenantId, String agentId, String version, String specYaml);

    void rollback(String agentId, String version);

    String getDeployStatus(String agentId, String version);
}
