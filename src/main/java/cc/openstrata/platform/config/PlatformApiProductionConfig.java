package cc.openstrata.platform.config;

import cc.openstrata.platform.domain.port.AgentRepository;
import cc.openstrata.platform.domain.port.AuditRecorder;
import cc.openstrata.platform.domain.port.CachePort;
import cc.openstrata.platform.domain.port.DeploymentPort;
import cc.openstrata.platform.domain.port.EvalPort;
import cc.openstrata.platform.domain.port.ModelRegistryPort;
import cc.openstrata.platform.domain.port.PlanRepository;
import cc.openstrata.platform.domain.port.SrsPort;
import cc.openstrata.platform.domain.port.TenantRepository;
import cc.openstrata.platform.domain.port.ToolRegistryPort;
import cc.openstrata.platform.infrastructure.adapter.HttpDeploymentAdapter;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.orm.jpa.JpaTransactionManager;
import cc.openstrata.platform.infrastructure.adapter.HttpEvalAdapter;
import cc.openstrata.platform.infrastructure.adapter.HttpSrsAdapter;
import cc.openstrata.platform.infrastructure.adapter.HttpToolRegistryAdapter;
import cc.openstrata.platform.infrastructure.adapter.JpaModelRegistryAdapter;
import cc.openstrata.platform.infrastructure.adapter.RedisCacheAdapter;
import cc.openstrata.platform.infrastructure.persistence.AgentJpaRepository;
import cc.openstrata.platform.infrastructure.persistence.AgentToolBindingJpaRepository;
import cc.openstrata.platform.infrastructure.persistence.AgentSkillBindingJpaRepository;
import cc.openstrata.platform.infrastructure.persistence.AgentVersionJpaRepository;
import cc.openstrata.platform.infrastructure.persistence.ApplicationJpaRepository;
import cc.openstrata.platform.infrastructure.persistence.AuditJpaRepository;
import cc.openstrata.platform.infrastructure.persistence.EvalRunJpaRepository;
import cc.openstrata.platform.infrastructure.persistence.JpaAgentRepository;
import cc.openstrata.platform.infrastructure.persistence.JpaAuditRecorder;
import cc.openstrata.platform.infrastructure.persistence.EntitlementJpaRepository;
import cc.openstrata.platform.infrastructure.persistence.JpaPlanRepository;
import cc.openstrata.platform.infrastructure.persistence.JpaTenantRepository;
import cc.openstrata.platform.infrastructure.persistence.ModelGrantJpaRepository;
import cc.openstrata.platform.infrastructure.persistence.ModelWhitelistJpaRepository;
import cc.openstrata.platform.infrastructure.persistence.PlanJpaRepository;
import cc.openstrata.platform.infrastructure.persistence.QuotaJpaRepository;
import cc.openstrata.platform.infrastructure.persistence.TenantJpaRepository;
import cc.openstrata.platform.infrastructure.persistence.UserJpaRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

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

    @Bean
    public AuditRecorder jpaAuditRecorder(AuditJpaRepository auditRepo) {
        return new JpaAuditRecorder(auditRepo);
    }

    @Bean
    public AgentRepository jpaAgentRepository(AgentJpaRepository agentRepo,
                                              AgentVersionJpaRepository versionRepo) {
        return new JpaAgentRepository(agentRepo, versionRepo);
    }

    // --- P2: real prod SPI adapters (replace the in-memory stand-ins) ---

    private RestClient.Builder clientBuilder(String baseUrl) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(3));
        factory.setReadTimeout(Duration.ofSeconds(15));
        return RestClient.builder().requestFactory(factory).baseUrl(baseUrl);
    }

    @Bean
    public ToolRegistryPort httpToolRegistryAdapter(OpenstrataProperties props,
            AgentToolBindingJpaRepository toolBindingRepo) {
        return new HttpToolRegistryAdapter(
                clientBuilder(props.getServices().getToolRegistry().getUrl()), toolBindingRepo);
    }

    @Bean
    public ModelRegistryPort jpaModelRegistryAdapter(ModelWhitelistJpaRepository whitelistRepo) {
        return new JpaModelRegistryAdapter(whitelistRepo);
    }

    @Bean
    public SrsPort httpSrsAdapter(OpenstrataProperties props,
            AgentSkillBindingJpaRepository skillBindingRepo) {
        return new HttpSrsAdapter(
                clientBuilder(props.getServices().getSrs().getUrl()), skillBindingRepo);
    }

    @Bean
    public EvalPort httpEvalAdapter(OpenstrataProperties props, EvalRunJpaRepository evalRunRepo) {
        return new HttpEvalAdapter(
                clientBuilder(props.getServices().getEval().getUrl()), evalRunRepo);
    }

    @Bean
    public DeploymentPort httpDeploymentAdapter(OpenstrataProperties props) {
        return new HttpDeploymentAdapter(
                clientBuilder(props.getServices().getProvisioning().getUrl()));
    }

    /**
     * Replaces the auto-configured JpaTransactionManager so every transaction is
     * scoped to the caller's tenant via RLS session variables (R-002). See
     * {@link RlsTransactionManager} and V5__rls_session.sql.
     */
    @Bean
    public JpaTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
        return new RlsTransactionManager(entityManagerFactory);
    }
}
