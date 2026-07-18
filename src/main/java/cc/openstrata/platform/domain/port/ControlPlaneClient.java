package cc.openstrata.platform.domain.port;

import java.util.Map;

/**
 * ControlPlaneClient SPI: the surface {@code ai-admin-service} consumes. On the
 * platform-api side it publishes domain snapshots to the governance orchestrator
 * (ACL: internal domain objects -> admin DTOs).
 */
public interface ControlPlaneClient {
    void publishTenantSnapshot(String tenantId, Map<String, Object> snapshot);
}
