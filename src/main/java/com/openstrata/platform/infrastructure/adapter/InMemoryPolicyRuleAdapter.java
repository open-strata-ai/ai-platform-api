package com.openstrata.platform.infrastructure.adapter;

import com.openstrata.platform.domain.port.PolicyRulePort;
import java.util.Map;

/** Offline PolicyRulePort (ai-srs-service) adapter — default approved, settable. */
public class InMemoryPolicyRuleAdapter implements PolicyRulePort {
    private boolean approved = true;

    public void setApproved(boolean approved) {
        this.approved = approved;
    }

    @Override
    public boolean isApproved(String tenantId, String operation, Map<String, Object> context) {
        return approved;
    }
}
