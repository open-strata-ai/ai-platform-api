package cc.openstrata.platform.config;

import cc.openstrata.platform.domain.port.CachePort;
import cc.openstrata.platform.domain.port.PlanRepository;
import cc.openstrata.platform.domain.port.TenantRepository;
import cc.openstrata.platform.infrastructure.adapter.RedisCacheAdapter;
import cc.openstrata.platform.infrastructure.persistence.ApplicationJpaRepository;
import cc.openstrata.platform.infrastructure.persistence.EntitlementJpaRepository;
import cc.openstrata.platform.infrastructure.persistence.JpaPlanRepository;
import cc.openstrata.platform.infrastructure.persistence.JpaTenantRepository;
import cc.openstrata.platform.infrastructure.persistence.ModelGrantJpaRepository;
import cc.openstrata.platform.infrastructure.persistence.PlanJpaRepository;
import cc.openstrata.platform.infrastructure.persistence.QuotaJpaRepository;
import cc.openstrata.platform.infrastructure.persistence.TenantJpaRepository;
import cc.openstrata.platform.infrastructure.persistence.UserJpaRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * Production-only beans activated by {@code SPRING_PROFILES_ACTIVE=prod}.
 * Replaces in-memory adapters with PostgreSQL/Redis-backed implementations.
 */
@Configuration
@Profile("prod")
public class PlatformApiProductionConfig {

    @Bean
    public TenantRepository jpaTenantRepository(TenantJpaRepository tenantRepo,
                                                 UserJpaRepository userRepo,
                                                 ApplicationJpaRepository appRepo,
                                                 QuotaJpaRepository quotaRepo,
                                                 EntitlementJpaRepository entitlementRepo,
                                                 ModelGrantJpaRepository modelGrantRepo) {
        return new JpaTenantRepository(tenantRepo, userRepo, appRepo, quotaRepo,
                entitlementRepo, modelGrantRepo);
    }

    @Bean
    public PlanRepository jpaPlanRepository(PlanJpaRepository planRepo, ObjectMapper mapper) {
        return new JpaPlanRepository(planRepo, mapper);
    }

    @Bean
    public CachePort redisCacheAdapter(StringRedisTemplate redis) {
        return new RedisCacheAdapter(redis);
    }
}
