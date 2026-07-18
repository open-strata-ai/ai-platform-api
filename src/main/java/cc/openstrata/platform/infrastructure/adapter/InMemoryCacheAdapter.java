package cc.openstrata.platform.infrastructure.adapter;

import cc.openstrata.platform.domain.port.CachePort;
import java.util.HashMap;
import java.util.Map;

/** Offline CachePort (Redis/Valkey) adapter with tenant-key isolation. */
public class InMemoryCacheAdapter implements CachePort {
    private final Map<String, String> store = new HashMap<>();

    @Override
    public void put(String tenantId, String key, String value) {
        store.put(tenantId + ":" + key, value);
    }

    @Override
    public String get(String tenantId, String key) {
        return store.get(tenantId + ":" + key);
    }
}
