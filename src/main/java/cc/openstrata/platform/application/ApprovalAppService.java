package cc.openstrata.platform.application;

import cc.openstrata.platform.application.dto.SubmitApprovalRequest;
import cc.openstrata.platform.config.TenantContext;
import cc.openstrata.platform.domain.DomainException;
import cc.openstrata.platform.domain.ErrorCode;
import cc.openstrata.platform.domain.Tenant;
import cc.openstrata.platform.domain.TenantId;
import cc.openstrata.platform.domain.rule.ApprovalRule;
import cc.openstrata.platform.domain.port.AuditRecorder;
import cc.openstrata.platform.domain.port.TenantRepository;
import java.util.Map;

/** High-risk operation approval use cases (RULE-06). */
public class ApprovalAppService {
    private final TenantRepository tenantRepository;
    private final ApprovalRule approvalRule;
    private final AuditRecorder auditRecorder;

    public ApprovalAppService(TenantRepository tenantRepository, ApprovalRule approvalRule,
                              AuditRecorder auditRecorder) {
        this.tenantRepository = tenantRepository;
        this.approvalRule = approvalRule;
        this.auditRecorder = auditRecorder;
    }

    private String actor() {
        TenantContext ctx = TenantContext.get();
        return ctx != null ? ctx.getActor() : "system";
    }

    public void submit(String tenantId, SubmitApprovalRequest req) {
        Tenant t = load(tenantId);
        if (approvalRule.isHighRisk(req.operation())) {
            approvalRule.requireApproval(tenantId, req.operation(), Map.of("tenant", tenantId));
        }
        auditRecorder.record(tenantId, actor(), "APPROVAL_SUBMITTED", "{\"operation\":\"" + req.operation() + "\"}");
    }

    public void approve(String tenantId, String operation) {
        load(tenantId);
        approvalRule.requireApproval(tenantId, operation, Map.of("tenant", tenantId));
        auditRecorder.record(tenantId, actor(), "APPROVAL_DECIDED", "{\"operation\":\"" + operation + "\"}");
    }

    private Tenant load(String tenantId) {
        return tenantRepository.findById(new TenantId(tenantId))
                .orElseThrow(() -> new DomainException(ErrorCode.TENANT_NOT_FOUND));
    }
}
