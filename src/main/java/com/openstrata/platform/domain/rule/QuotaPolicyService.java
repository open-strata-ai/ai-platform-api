package com.openstrata.platform.domain.rule;

import com.openstrata.platform.domain.Plan;
import com.openstrata.platform.domain.QuotaTemplate;

/**
 * Computes per-dimension quota limits from a plan's template. GPU quota only
 * takes effect when the tenant enables self-hosted inference (full profile).
 */
public class QuotaPolicyService {
    public QuotaTemplate computePolicy(Plan plan, boolean modelServingEnabled) {
        QuotaTemplate t = plan.getQuotaTemplate();
        if (!modelServingEnabled) {
            return t.disableGpu();
        }
        return t;
    }
}
