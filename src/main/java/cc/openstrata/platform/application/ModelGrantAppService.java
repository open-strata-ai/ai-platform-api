package cc.openstrata.platform.application;

import cc.openstrata.platform.application.dto.GrantModelRequest;
import cc.openstrata.platform.config.TenantContext;
import cc.openstrata.platform.domain.DomainException;
import cc.openstrata.platform.domain.ErrorCode;
import cc.openstrata.platform.domain.Plan;
import cc.openstrata.platform.domain.PlanId;
import cc.openstrata.platform.domain.Tenant;
import cc.openstrata.platform.domain.TenantId;
import cc.openstrata.platform.domain.rule.ModelGrantRule;
import cc.openstrata.platform.domain.port.AuditRecorder;
import cc.openstrata.platform.domain.port.PlanRepository;
import cc.openstrata.platform.domain.port.TenantRepository;

/** Vendor model grant use cases (RULE-05). */
public class ModelGrantAppService {
    private final TenantRepository tenantRepository;
    private final PlanRepository planRepository;
    private final ModelGrantRule modelGrantRule;
    private final AuditRecorder auditRecorder;

    public ModelGrantAppService(TenantRepository tenantRepository, PlanRepository planRepository,
                                ModelGrantRule modelGrantRule, AuditRecorder auditRecorder) {
        this.tenantRepository = tenantRepository;
        this.planRepository = planRepository;
        this.modelGrantRule = modelGrantRule;
        this.auditRecorder = auditRecorder;
    }

    private String actor() {
        TenantContext ctx = TenantContext.get();
        return ctx != null ? ctx.getActor() : "system";
    }

    public void grant(String tenantId, GrantModelRequest req) {
        Tenant t = load(tenantId);
        Plan plan = t.getPlanId() == null ? null : planRepository.findById(t.getPlanId()).orElse(null);
        modelGrantRule.authorize(plan, req.provider(), req.model());
        t.grantModel(req.provider(), req.model());
        tenantRepository.save(t);
        auditRecorder.record(tenantId, actor(), "MODEL_GRANTED", "{\"model\":\"" + req.model() + "\"}");
    }

    private Tenant load(String tenantId) {
        return tenantRepository.findById(new TenantId(tenantId))
                .orElseThrow(() -> new DomainException(ErrorCode.TENANT_NOT_FOUND));
    }
}
