package com.openstrata.platform.domain.rule;

import com.openstrata.platform.domain.DomainException;
import com.openstrata.platform.domain.ErrorCode;

/** RULE-03: multitenancy requires auth to be active first (§12.4 dependency). */
public class MultitenancyProvisioningRule {
    public void validate(boolean multitenancyEnabled, boolean authEnabled) {
        if (multitenancyEnabled && !authEnabled) {
            throw new DomainException(ErrorCode.MULTITENANCY_REQUIRES_AUTH,
                    "auth must be enabled before multitenancy");
        }
    }
}
