package cc.openstrata.platform.domain.port;

/** Cache SPI (Redis / Valkey). Tenant-key-prefixed isolation. */
public interface CachePort {
    void put(String tenantId, String key, String value);

    String get(String tenantId, String key);
}
