package com.openstrata.platform.application;

import com.openstrata.platform.application.dto.AssignPlanRequest;
import com.openstrata.platform.application.dto.UpdateQuotaRequest;
import com.openstrata.platform.application.dto.GrantModelRequest;
import com.openstrata.platform.domain.DomainException;
import com.openstrata.platform.domain.ResourceDimension;
import com.openstrata.platform.domain.Tenant;
import com.openstrata.platform.domain.TenantId;
import com.openstrata.platform.domain.rule.ApprovalRule;
import com.openstrata.platform.domain.rule.QuotaPolicyService;
import com.openstrata.platform.infrastructure.adapter.InMemoryAppRegistryAdapter;
import com.openstrata.platform.infrastructure.adapter.InMemoryAuditRecorder;
import com.openstrata.platform.infrastructure.adapter.InMemoryBillingEventAdapter;
import com.openstrata.platform.infrastructure.adapter.InMemoryControlPlaneClient;
import com.openstrata.platform.infrastructure.adapter.InMemoryPolicyRuleAdapter;
import com.openstrata.platform.infrastructure.persistence.InMemoryPlanRepository;
import com.openstrata.platform.infrastructure.persistence.InMemoryTenantRepository;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QuotaAppServiceTest {
    private final InMemoryTenantRepository repo = new InMemoryTenantRepository();
    private final InMemoryPlanRepository plans = new InMemoryPlanRepository();
    private final InMemoryAppRegistryAdapter appReg = new InMemoryAppRegistryAdapter();
    private final InMemoryBillingEventAdapter billing = new InMemoryBillingEventAdapter();
    private final InMemoryAuditRecorder audit = new InMemoryAuditRecorder();
    private final InMemoryPolicyRuleAdapter policy = new InMemoryPolicyRuleAdapter();
    private final QuotaAppService quotaService = new QuotaAppService(repo, plans, appReg,
            billing, new ApprovalRule(policy), audit);
    private final PlanAppService planService = new PlanAppService(repo, plans,
            new QuotaPolicyService(), appReg, new InMemoryControlPlaneClient(), audit);

    private String seeded() {
        String id = "t1";
        repo.save(Tenant.create(new TenantId(id), "Acme"));
        return id;
    }

    @Test
    void gpuQuotaRejectedWithoutEnterprisePlan() {
        String id = seeded();
        assertThrows(DomainException.class,
                () -> quotaService.updateQuota(id, new UpdateQuotaRequest("GPU", 4)));
    }

    @Test
    void gpuQuotaAllowedAfterEnterprisePlan() {
        String id = seeded();
        policy.setApproved(true);
        planService.assignPlan(id, new AssignPlanRequest("enterprise"));
        quotaService.updateQuota(id, new UpdateQuotaRequest("GPU", 4));
        Tenant t = repo.findById(new TenantId(id)).orElseThrow();
        assertTrue(t.getQuotas().stream()
                .anyMatch(q -> q.getDimension() == ResourceDimension.GPU && q.getLimit() == 4));
    }
}
