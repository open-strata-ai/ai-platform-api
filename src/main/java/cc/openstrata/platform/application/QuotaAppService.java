package cc.openstrata.platform.application;

import cc.openstrata.platform.application.dto.UpdateQuotaRequest;
import cc.openstrata.platform.config.TenantContext;
import cc.openstrata.platform.domain.DomainException;
import cc.openstrata.platform.domain.ErrorCode;
import cc.openstrata.platform.domain.Plan;
import cc.openstrata.platform.domain.PlanId;
import cc.openstrata.platform.domain.ResourceDimension;
import cc.openstrata.platform.domain.Tenant;
import cc.openstrata.platform.domain.TenantId;
import cc.openstrata.platform.domain.rule.ApprovalRule;
import cc.openstrata.platform.domain.port.AppRegistryPort;
import cc.openstrata.platform.domain.port.AuditRecorder;
import cc.openstrata.platform.domain.port.BillingEventPort;
import cc.openstrata.platform.domain.port.PlanRepository;
import cc.openstrata.platform.domain.port.TenantRepository;
import java.util.Map;

/** Quota adjustment use cases. */
public class QuotaAppService {
    private final TenantRepository tenantRepository;
    private final PlanRepository planRepository;
    private final AppRegistryPort appRegistryPort;
    private final BillingEventPort billingEventPort;
    private final ApprovalRule approvalRule;
    private final AuditRecorder auditRecorder;

    public QuotaAppService(TenantRepository tenantRepository, PlanRepository planRepository,
                           AppRegistryPort appRegistryPort, BillingEventPort billingEventPort,
                           ApprovalRule approvalRule, AuditRecorder auditRecorder) {
        this.tenantRepository = tenantRepository;
        this.planRepository = planRepository;
        this.appRegistryPort = appRegistryPort;
        this.billingEventPort = billingEventPort;
        this.approvalRule = approvalRule;
        this.auditRecorder = auditRecorder;
    }

    private String actor() {
        TenantContext ctx = TenantContext.get();
        return ctx != null ? ctx.getActor() : "system";
    }

    public void updateQuota(String tenantId, UpdateQuotaRequest req) {
        Tenant t = load(tenantId);
        ResourceDimension dim = ResourceDimension.valueOf(req.dimension().toUpperCase());
        if (dim == ResourceDimension.GPU && !isEnterprise(t)) {
            throw new DomainException(ErrorCode.TENANT_QUOTA_GPU_DISABLED);
        }
        approvalRule.requireApproval(tenantId, "QUOTA_INCREASE", Map.of("tenant", tenantId, "dimension", dim.name()));
        t.setQuota(dim, req.limit());
        tenantRepository.save(t);
        appRegistryPort.pushQuota(tenantId, Map.of(dim, req.limit()));
        billingEventPort.quotaChanged(tenantId);
        auditRecorder.record(tenantId, actor(), "QUOTA_UPDATED", "{\"dimension\":\"" + dim + "\"}");
    }

    private boolean isEnterprise(Tenant t) {
        if (t.getPlanId() == null) {
            return false;
        }
        return planRepository.findById(t.getPlanId()).map(Plan::isEnterprise).orElse(false);
    }

    private Tenant load(String tenantId) {
        return tenantRepository.findById(new TenantId(tenantId))
                .orElseThrow(() -> new DomainException(ErrorCode.TENANT_NOT_FOUND));
    }
}
