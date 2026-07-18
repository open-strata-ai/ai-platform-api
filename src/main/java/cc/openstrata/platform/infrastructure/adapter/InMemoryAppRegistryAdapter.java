package cc.openstrata.platform.infrastructure.adapter;

import cc.openstrata.platform.domain.ResourceDimension;
import cc.openstrata.platform.domain.port.AppRegistryPort;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Offline AppRegistryPort adapter — fan-out to gateway/tool-registry. */
public class InMemoryAppRegistryAdapter implements AppRegistryPort {
    public final List<String> apps = new ArrayList<>();
    public final List<String> quotas = new ArrayList<>();

    @Override
    public void registerApplication(String tenantId, String appId, String name) {
        apps.add(tenantId + ":" + appId);
    }

    @Override
    public void unregisterApplication(String tenantId, String appId) {
        // no-op for offline
    }

    @Override
    public void pushQuota(String tenantId, Map<ResourceDimension, Long> quotas) {
        this.quotas.add(tenantId + ":" + quotas.size());
    }
}
