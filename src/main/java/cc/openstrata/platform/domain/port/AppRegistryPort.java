package cc.openstrata.platform.domain.port;

import cc.openstrata.platform.domain.ResourceDimension;
import java.util.Map;

/** AppRegistry SPI: fan-out to ai-gateway-core (quota) + ai-tool-registry (apps). */
public interface AppRegistryPort {
    void registerApplication(String tenantId, String appId, String name);

    void unregisterApplication(String tenantId, String appId);

    void pushQuota(String tenantId, Map<ResourceDimension, Long> quotas);
}
