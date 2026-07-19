package cc.openstrata.platform.infrastructure.adapter;

import cc.openstrata.platform.domain.port.CachePort;
import java.time.Duration;
import org.springframework.data.redis.core.StringRedisTemplate;

/** Redis-backed CachePort. Activated by @Profile("prod"). */
public class RedisCacheAdapter implements CachePort {

    private final StringRedisTemplate redis;

    /** Default TTL: 5 minutes. */
    private static final Duration TTL = Duration.ofMinutes(5);

    public RedisCacheAdapter(StringRedisTemplate redis) {
        this.redis = redis;
    }

    private String key(String tenantId, String k) {
        return "platform:" + tenantId + ":" + k;
    }

    @Override
    public void put(String tenantId, String key, String value) {
        redis.opsForValue().set(key(tenantId, key), value, TTL);
    }

    @Override
    public String get(String tenantId, String key) {
        return redis.opsForValue().get(key(tenantId, key));
    }
}
