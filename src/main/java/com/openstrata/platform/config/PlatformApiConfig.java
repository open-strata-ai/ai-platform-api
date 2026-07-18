package com.openstrata.platform.config;

import com.openstrata.platform.application.ApprovalAppService;
import com.openstrata.platform.application.ApplicationAppService;
import com.openstrata.platform.application.EntitlementAppService;
import com.openstrata.platform.application.ModelGrantAppService;
import com.openstrata.platform.application.PlanAppService;
import com.openstrata.platform.application.QuotaAppService;
import com.openstrata.platform.application.TenantAppService;
import com.openstrata.platform.application.TenantQueryService;
import com.openstrata.platform.application.UserAppService;
import com.openstrata.platform.domain.rule.ApprovalRule;
import com.openstrata.platform.domain.rule.EntitlementConsistencyRule;
import com.openstrata.platform.domain.rule.ModelGrantRule;
import com.openstrata.platform.domain.rule.MultitenancyProvisioningRule;
import com.openstrata.platform.domain.rule.QuotaPolicyService;
import com.openstrata.platform.domain.rule.TenantIdUniquenessRule;
import com.openstrata.platform.domain.port.AuthPort;
import com.openstrata.platform.domain.port.AppRegistryPort;
import com.openstrata.platform.domain.port.AuditRecorder;
import com.openstrata.platform.domain.port.BillingEventPort;
import com.openstrata.platform.domain.port.ControlPlaneClient;
import com.openstrata.platform.domain.port.ManifestPort;
import com.openstrata.platform.domain.port.MultiTenancyPort;
import com.openstrata.platform.domain.port.PlanRepository;
import com.openstrata.platform.domain.port.PolicyRulePort;
import com.openstrata.platform.domain.port.TenantRepository;
import com.openstrata.platform.infrastructure.adapter.InMemoryAppRegistryAdapter;
import com.openstrata.platform.infrastructure.adapter.InMemoryAuditRecorder;
import com.openstrata.platform.infrastructure.adapter.InMemoryAuthAdapter;
import com.openstrata.platform.infrastructure.adapter.InMemoryBillingEventAdapter;
import com.openstrata.platform.infrastructure.adapter.InMemoryControlPlaneClient;
import com.openstrata.platform.infrastructure.adapter.InMemoryManifestAdapter;
import com.openstrata.platform.infrastructure.adapter.InMemoryMultiTenancyAdapter;
import com.openstrata.platform.infrastructure.adapter.InMemoryPolicyRuleAdapter;
import com.openstrata.platform.infrastructure.persistence.InMemoryPlanRepository;
import com.openstrata.platform.infrastructure.persistence.InMemoryTenantRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PlatformApiConfig {

    @Bean
    public TenantRepository tenantRepository() {
        return new InMemoryTenantRepository();
    }

    @Bean
    public PlanRepository planRepository() {
        return new InMemoryPlanRepository();
    }

    @Bean
    public AuthPort authPort() {
        return new InMemoryAuthAdapter();
    }

    @Bean
    public MultiTenancyPort multiTenancyPort() {
        return new InMemoryMultiTenancyAdapter();
    }

    @Bean
    public ManifestPort manifestPort() {
        return new InMemoryManifestAdapter();
    }

    @Bean
    public AppRegistryPort appRegistryPort() {
        return new InMemoryAppRegistryAdapter();
    }

    @Bean
    public PolicyRulePort policyRulePort() {
        return new InMemoryPolicyRuleAdapter();
    }

    @Bean
    public BillingEventPort billingEventPort() {
        return new InMemoryBillingEventAdapter();
    }

    @Bean
    public ControlPlaneClient controlPlaneClient() {
        return new InMemoryControlPlaneClient();
    }

    @Bean
    public AuditRecorder auditRecorder() {
        return new InMemoryAuditRecorder();
    }

    @Bean
    public TenantIdUniquenessRule tenantIdUniquenessRule(TenantRepository tenantRepository) {
        return new TenantIdUniquenessRule(tenantRepository);
    }

    @Bean
    public QuotaPolicyService quotaPolicyService() {
        return new QuotaPolicyService();
    }

    @Bean
    public MultitenancyProvisioningRule multitenancyProvisioningRule() {
        return new MultitenancyProvisioningRule();
    }

    @Bean
    public EntitlementConsistencyRule entitlementConsistencyRule() {
        return new EntitlementConsistencyRule();
    }

    @Bean
    public ModelGrantRule modelGrantRule() {
        return new ModelGrantRule();
    }

    @Bean
    public ApprovalRule approvalRule(PolicyRulePort policyRulePort) {
        return new ApprovalRule(policyRulePort);
    }

    @Bean
    public TenantAppService tenantAppService(TenantRepository tenantRepository,
                                             TenantIdUniquenessRule tenantIdUniquenessRule, AuthPort authPort,
                                             MultiTenancyPort multiTenancyPort, ManifestPort manifestPort,
                                             AuditRecorder auditRecorder, OpenstrataProperties properties,
                                             ControlPlaneClient controlPlaneClient) {
        return new TenantAppService(tenantRepository, tenantIdUniquenessRule, authPort, multiTenancyPort,
                manifestPort, auditRecorder, properties, controlPlaneClient);
    }

    @Bean
    public UserAppService userAppService(TenantRepository tenantRepository, AuthPort authPort,
                                         AuditRecorder auditRecorder) {
        return new UserAppService(tenantRepository, authPort, auditRecorder);
    }

    @Bean
    public ApplicationAppService applicationAppService(TenantRepository tenantRepository,
                                                      AppRegistryPort appRegistryPort, AuditRecorder auditRecorder) {
        return new ApplicationAppService(tenantRepository, appRegistryPort, auditRecorder);
    }

    @Bean
    public PlanAppService planAppService(TenantRepository tenantRepository, PlanRepository planRepository,
                                         QuotaPolicyService quotaPolicyService, AppRegistryPort appRegistryPort,
                                         ControlPlaneClient controlPlaneClient, AuditRecorder auditRecorder) {
        return new PlanAppService(tenantRepository, planRepository, quotaPolicyService, appRegistryPort,
                controlPlaneClient, auditRecorder);
    }

    @Bean
    public QuotaAppService quotaAppService(TenantRepository tenantRepository, PlanRepository planRepository,
                                           AppRegistryPort appRegistryPort, BillingEventPort billingEventPort,
                                           ApprovalRule approvalRule, AuditRecorder auditRecorder) {
        return new QuotaAppService(tenantRepository, planRepository, appRegistryPort, billingEventPort,
                approvalRule, auditRecorder);
    }

    @Bean
    public EntitlementAppService entitlementAppService(TenantRepository tenantRepository,
                                                       EntitlementConsistencyRule entitlementConsistencyRule,
                                                       ManifestPort manifestPort, BillingEventPort billingEventPort,
                                                       AuditRecorder auditRecorder) {
        return new EntitlementAppService(tenantRepository, entitlementConsistencyRule, manifestPort,
                billingEventPort, auditRecorder);
    }

    @Bean
    public ModelGrantAppService modelGrantAppService(TenantRepository tenantRepository,
                                                     PlanRepository planRepository, ModelGrantRule modelGrantRule,
                                                     AuditRecorder auditRecorder) {
        return new ModelGrantAppService(tenantRepository, planRepository, modelGrantRule, auditRecorder);
    }

    @Bean
    public ApprovalAppService approvalAppService(TenantRepository tenantRepository, ApprovalRule approvalRule,
                                                 AuditRecorder auditRecorder) {
        return new ApprovalAppService(tenantRepository, approvalRule, auditRecorder);
    }

    @Bean
    public TenantQueryService tenantQueryService(TenantRepository tenantRepository) {
        return new TenantQueryService(tenantRepository);
    }
}
