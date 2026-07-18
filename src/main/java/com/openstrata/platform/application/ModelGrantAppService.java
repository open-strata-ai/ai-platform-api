package com.openstrata.platform.application;

import com.openstrata.platform.application.dto.GrantModelRequest;
import com.openstrata.platform.config.TenantContext;
import com.openstrata.platform.domain.DomainException;
import com.openstrata.platform.domain.ErrorCode;
import com.openstrata.platform.domain.Plan;
import com.openstrata.platform.domain.PlanId;
import com.openstrata.platform.domain.Tenant;
import com.openstrata.platform.domain.TenantId;
import com.openstrata.platform.domain.rule.ModelGrantRule;
import com.openstrata.platform.domain.port.AuditRecorder;
import com.openstrata.platform.domain.port.PlanRepository;
import com.openstrata.platform.domain.port.TenantRepository;

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
