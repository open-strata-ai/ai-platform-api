package com.openstrata.platform.domain.rule;

import com.openstrata.platform.domain.DomainException;
import com.openstrata.platform.domain.ErrorCode;
import com.openstrata.platform.domain.Plan;

/** RULE-05: restricted vendor models require an Enterprise plan. */
public class ModelGrantRule {
    public void authorize(Plan plan, String provider, String model) {
        if (isRestricted(model) && (plan == null || !plan.isEnterprise())) {
            throw new DomainException(ErrorCode.MODEL_RESTRICTED,
                    "Model " + model + " requires Enterprise plan");
        }
    }

    private boolean isRestricted(String model) {
        return "gpt-4o".equalsIgnoreCase(model);
    }
}
