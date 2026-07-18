package com.openstrata.platform.domain;

/** Predefined plan tier (Trial/Standard/Enterprise) carrying a QuotaTemplate. */
public class Plan {
    private final PlanId planId;
    private final String name;
    private final QuotaTemplate quotaTemplate;
    private final boolean enterprise;

    public Plan(PlanId planId, String name, QuotaTemplate quotaTemplate, boolean enterprise) {
        this.planId = planId;
        this.name = name;
        this.quotaTemplate = quotaTemplate;
        this.enterprise = enterprise;
    }

    public PlanId getPlanId() {
        return planId;
    }

    public String getName() {
        return name;
    }

    public QuotaTemplate getQuotaTemplate() {
        return quotaTemplate;
    }

    public boolean isEnterprise() {
        return enterprise;
    }
}
