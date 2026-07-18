package com.openstrata.platform.domain.rule;

import com.openstrata.platform.domain.DomainException;
import com.openstrata.platform.domain.ErrorCode;
import com.openstrata.platform.domain.port.PolicyRulePort;
import java.util.Map;

/** RULE-06: high-risk operations must pass ApprovalDecided (via PolicyRulePort). */
public class ApprovalRule {
    private final PolicyRulePort policyRulePort;

    public ApprovalRule(PolicyRulePort policyRulePort) {
        this.policyRulePort = policyRulePort;
    }

    public boolean isHighRisk(String operation) {
        return "DELETE_TENANT".equals(operation) || "QUOTA_INCREASE".equals(operation);
    }

    public void requireApproval(String tenantId, String operation, Map<String, Object> context) {
        if (!policyRulePort.isApproved(tenantId, operation, context)) {
            throw new DomainException(ErrorCode.APPROVAL_REQUIRED,
                    "High-risk operation " + operation + " requires approval");
        }
    }
}
