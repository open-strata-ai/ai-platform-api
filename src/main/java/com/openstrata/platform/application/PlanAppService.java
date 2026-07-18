package com.openstrata.platform.application;

import com.openstrata.platform.application.dto.AssignPlanRequest;
import com.openstrata.platform.config.TenantContext;
import com.openstrata.platform.domain.DomainException;
import com.openstrata.platform.domain.ErrorCode;
import com.openstrata.platform.domain.Plan;
import com.openstrata.platform.domain.PlanId;
import com.openstrata.platform.domain.QuotaTemplate;
import com.openstrata.platform.domain.ResourceDimension;
import com.openstrata.platform.domain.Tenant;
import com.openstrata.platform.domain.TenantId;
import com.openstrata.platform.domain.rule.QuotaPolicyService;
import com.openstrata.platform.domain.port.AppRegistryPort;
import com.openstrata.platform.domain.port.AuditRecorder;
import com.openstrata.platform.domain.port.ControlPlaneClient;
import com.openstrata.platform.domain.port.PlanRepository;
import com.openstrata.platform.domain.port.TenantRepository;
import java.util.EnumMap;
import java.util.Map;

/** Plan assignment + quota provisioning use cases. */
public class PlanAppService {
    private final TenantRepository tenantRepository;
    private final PlanRepository planRepository;
    private final QuotaPolicyService quotaPolicyService;
    private final AppRegistryPort appRegistryPort;
    private final ControlPlaneClient controlPlaneClient;
    private final AuditRecorder auditRecorder;

    public PlanAppService(TenantRepository tenantRepository, PlanRepository planRepository,
                          QuotaPolicyService quotaPolicyService, AppRegistryPort appRegistryPort,
                          ControlPlaneClient controlPlaneClient, AuditRecorder auditRecorder) {
        this.tenantRepository = tenantRepository;
        this.planRepository = planRepository;
        this.quotaPolicyService = quotaPolicyService;
        this.appRegistryPort = appRegistryPort;
        this.controlPlaneClient = controlPlaneClient;
        this.auditRecorder = auditRecorder;
    }

    private String actor() {
        TenantContext ctx = TenantContext.get();
        return ctx != null ? ctx.getActor() : "system";
    }

    public void assignPlan(String tenantId, AssignPlanRequest req) {
        Tenant t = load(tenantId);
        Plan plan = planRepository.findById(new PlanId(req.planId()))
                .orElseThrow(() -> new DomainException(ErrorCode.ILLEGAL_ARGUMENT, "unknown plan " + req.planId()));
        t.assignPlan(plan.getPlanId());
        // GPU quota only effective when self-hosted inference (full profile); proxy = enterprise tier.
        QuotaTemplate policy = quotaPolicyService.computePolicy(plan, plan.isEnterprise());
        EnumMap<ResourceDimension, Long> quotaMap = new EnumMap<>(ResourceDimension.class);
        quotaMap.put(ResourceDimension.CPU, policy.cpu());
        quotaMap.put(ResourceDimension.TOKEN, policy.token());
        quotaMap.put(ResourceDimension.QPS, policy.qps());
        quotaMap.put(ResourceDimension.VECTOR, policy.vector());
        quotaMap.put(ResourceDimension.GPU, policy.gpu());
        quotaMap.forEach((dim, val) -> t.setQuota(dim, val));
        tenantRepository.save(t);
        appRegistryPort.pushQuota(tenantId, quotaMap);
        controlPlaneClient.publishTenantSnapshot(tenantId, Map.of("plan", plan.getName()));
        auditRecorder.record(tenantId, actor(), "PLAN_ASSIGNED", "{\"plan\":\"" + plan.getName() + "\"}");
    }

    private Tenant load(String tenantId) {
        return tenantRepository.findById(new TenantId(tenantId))
                .orElseThrow(() -> new DomainException(ErrorCode.TENANT_NOT_FOUND));
    }
}
