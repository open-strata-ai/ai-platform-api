package com.openstrata.platform.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * Tenant aggregate root. Encapsulates the consistency boundary for users,
 * applications, plan, quotas, entitlements and model grants (DESIGN §3/§4.2).
 * All sub-entity mutations go through this root.
 */
public class Tenant {
    private final TenantId tenantId;
    private String name;
    private TenantStatus status;
    private PlanId planId;
    private boolean multitenancyEnabled;

    private final List<User> users = new ArrayList<>();
    private final List<Application> applications = new ArrayList<>();
    private final List<Quota> quotas = new ArrayList<>();
    private final List<Entitlement> entitlements = new ArrayList<>();
    private final List<ModelGrant> modelGrants = new ArrayList<>();

    public Tenant(TenantId tenantId, String name) {
        this.tenantId = tenantId;
        this.name = name;
        this.status = TenantStatus.PROVISIONING;
        this.multitenancyEnabled = false;
    }

    public static Tenant create(TenantId tenantId, String name) {
        return new Tenant(tenantId, name);
    }

    public void activate() {
        this.status = TenantStatus.ACTIVE;
    }

    public void suspend() {
        this.status = TenantStatus.SUSPENDED;
    }

    public void resume() {
        this.status = TenantStatus.ACTIVE;
    }

    public void markDeleted() {
        this.status = TenantStatus.DELETED;
    }

    public void enableMultitenancy() {
        this.multitenancyEnabled = true;
    }

    public void assignPlan(PlanId planId) {
        this.planId = planId;
    }

    public void addUser(User user) {
        this.users.add(user);
    }

    public User findUser(UserId userId) {
        return users.stream()
                .filter(u -> u.getUserId().equals(userId))
                .findFirst()
                .orElseThrow(() -> new DomainException(ErrorCode.TENANT_NOT_FOUND,
                        "User " + userId.value() + " not found"));
    }

    public void changeUserRole(UserId userId, Role role) {
        findUser(userId).changeRole(role);
    }

    public void registerApplication(Application app) {
        this.applications.add(app);
    }

    public void setQuota(ResourceDimension dimension, long limit) {
        quotas.stream()
                .filter(q -> q.getDimension() == dimension)
                .findFirst()
                .ifPresentOrElse(q -> q.setLimit(limit),
                        () -> quotas.add(new Quota(new QuotaId(tenantId.value() + "-" + dimension), tenantId, dimension, limit)));
    }

    public void setEntitlement(String component, boolean allowed) {
        entitlements.stream()
                .filter(e -> e.getComponent().equals(component))
                .findFirst()
                .ifPresentOrElse(e -> e.setAllowed(allowed),
                        () -> entitlements.add(new Entitlement(component, allowed)));
    }

    public void grantModel(String provider, String model) {
        if (modelGrants.stream().noneMatch(m -> m.getProvider().equals(provider) && m.getModel().equals(model))) {
            modelGrants.add(new ModelGrant(provider, model));
        }
    }

    public TenantId getTenantId() {
        return tenantId;
    }

    public String getName() {
        return name;
    }

    public TenantStatus getStatus() {
        return status;
    }

    public PlanId getPlanId() {
        return planId;
    }

    public boolean isMultitenancyEnabled() {
        return multitenancyEnabled;
    }

    public List<User> getUsers() {
        return List.copyOf(users);
    }

    public List<Application> getApplications() {
        return List.copyOf(applications);
    }

    public List<Quota> getQuotas() {
        return List.copyOf(quotas);
    }

    public List<Entitlement> getEntitlements() {
        return List.copyOf(entitlements);
    }

    public List<ModelGrant> getModelGrants() {
        return List.copyOf(modelGrants);
    }
}
