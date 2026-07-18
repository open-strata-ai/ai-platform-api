# ai-platform-api · AI Coding Rules & Skills (SKILLS)

> **Source**: Extracted from `docs/DESIGN.md` §5 (Domain Rules), §11 (Integration Points), §12 (Security & Multi-tenancy). These rules guide AI-assisted development within this repo.

---

## RULE-01: TenantId Uniqueness Enforcement

| Aspect | Detail |
| --- | --- |
| **Trigger** | Creating or importing a tenant, or writing any domain logic that generates a `tenant_id`. |
| **Constraint** | `tenant_id` MUST be globally unique across the entire platform. Enforced by PostgreSQL `UNIQUE` constraint + domain-level validation. |
| **Rationale** | `tenant_id` spans all resources (users, apps, quotas, entitlements); duplicates corrupt the control plane. |

**Implementation pattern**:
```java
// Domain service: TenantIdUniquenessRule
// Checks uniqueness before aggregate creation; also relies on DB constraint.
tenantRepository.findByTenantId(command.getTenantId())
    .ifPresent(t -> { throw new TenantIdConflictException(command.getTenantId()); });
```

**Test checklist**:
- [ ] Unit test: `TenantIdUniquenessRule.reject()` throws for duplicate.
- [ ] Integration test: concurrent creation returns `409 TENANT_ID_CONFLICT`.

---

## RULE-02: Quota Policy Computation from Plan Template

| Aspect | Detail |
| --- | --- |
| **Trigger** | Assigning a plan (`PlanAppService.assignPlan()`) or changing quota limits. |
| **Constraint** | Compute per-dimension upper limits from `Plan.quotaTemplate`. Token/QPS/Vector quotas are governed starting from `advanced` profile. GPU quota only takes effect when tenant enables self-hosted inference (`full` profile, `modelServing.enabled`). |
| **Rationale** | Prevents GPU quota leakage to tenants without self-hosted GPU infrastructure (§8.1 D5, §14.4). |

**Implementation pattern**:
```java
// Domain service: QuotaPolicyService
public QuotaPolicy computePolicy(Plan plan, boolean modelServingEnabled) {
    QuotaPolicy policy = new QuotaPolicy(plan.getQuotaTemplate());
    if (!modelServingEnabled) {
        policy.disableDimension(ResourceDimension.GPU);  // GPU quota not applicable
    }
    return policy;
}
```

**Test checklist**:
- [ ] Trial plan → correctly maps to trial quotas.
- [ ] Enterprise plan without `modelServing` → GPU dimension is zeroed/disabled.
- [ ] Enterprise plan + `modelServing` → GPU quota active.

---

## RULE-03: Multi-tenancy Enablement Preconditions

| Aspect | Detail |
| --- | --- |
| **Trigger** | Calling `TenantAppService.enableMultitenancy()`. |
| **Constraint** | Multi-tenancy can only be enabled if BOTH: (1) `multitenancy.enabled=true`, AND (2) `auth` is already active (§12.4 dependency check). After lighting up: enforce "data not leaving tenant" isolation. |
| **Rationale** | `multitenancy` depends on `auth` (§12.4 dependency graph). Enabling without auth creates an orphaned isolation boundary. |

**Implementation pattern**:
```java
// Domain service: MultitenancyProvisioningRule
public void validate(boolean multitenancyEnabled, boolean authEnabled) {
    if (!authEnabled) {
        throw new MultitenancyPreconditionException("auth must be enabled first");
    }
    // Proceed: create Capsule Tenant → Namespace → NetworkPolicy → data prefixes
}
```

**Test checklist**:
- [ ] `enableMultitenancy()` with `auth=false` → `422 MULTITENANCY_REQUIRES_AUTH`.
- [ ] `enableMultitenancy()` with `auth=true` → success, Capsule Tenant created.

---

## RULE-04: Entitlement Dependency Compatibility

| Aspect | Detail |
| --- | --- |
| **Trigger** | Setting component whitelist (`EntitlementAppService.setEntitlements()`). |
| **Constraint** | Component whitelist must be compatible with `PlatformManifest` dependency graph (§12.4 table). Specifically: `billing` requires `multitenancy` to be enabled; `multitenancy` requires `auth`. |
| **Rationale** | Prevents enabling billing without the multi-tenant isolation layer it depends on. |

**Implementation pattern**:
```java
// Domain service: EntitlementConsistencyRule
public void validate(Set<ComponentKey> entitlements, PlatformManifest manifest) {
    if (entitlements.contains(ComponentKey.BILLING)
        && !entitlements.contains(ComponentKey.MULTITENANCY)) {
        throw new EntitlementDependencyException("billing requires multitenancy");
    }
    // Validate full dependency graph
    manifest.validateDependencies(entitlements);
}
```

---

## RULE-05: Model Grant Restricted to Enterprise Tenants

| Aspect | Detail |
| --- | --- |
| **Trigger** | Granting a restricted model (e.g., GPT-4o) to a tenant. |
| **Constraint** | Only Enterprise-tier tenants may be authorized for restricted vendor models (§14.2). Authorization semantics come through `LLMProvider` SPI (§4.4.4). |
| **Rationale** | Vendor contracts and cost profiles limit premium models to Enterprise plan. |

**Implementation pattern**:
```java
// Domain service: ModelGrantRule
public void authorize(Tenant tenant, String provider, String model) {
    if (isRestrictedModel(provider, model) && !tenant.getPlan().isEnterprise()) {
        throw new ModelGrantRestrictedException(
            "Model " + model + " requires Enterprise plan");
    }
    tenant.grantModel(provider, model);
}
```

---

## RULE-06: Approval Flow for High-Risk Operations

| Aspect | Detail |
| --- | --- |
| **Trigger** | Performing a high-risk operation: deleting a tenant, exceeding budget quota increase. |
| **Constraint** | Must pass `ApprovalDecided` before the operation takes effect. Approval rules can be loaded from `ai-srs-service` via `PolicyRulePort` as OPA/Drools policies. |
| **Rationale** | Prevents irreversible damage from single-admin actions; SRS-backed rules allow tenant-specific approval policies. |

---

## RULE-07: SPI Adapter Contract Enforcement (bump-spi-version)

| Aspect | Detail |
| --- | --- |
| **Trigger** | Upgrading an external dependency (Keycloak, Capsule, Redis/Valkey) or changing a SPI interface. |
| **Constraint** | Must update `bom.yaml` `interface_versions` and run SPI contract tests against all Adapter implementations. Contract tests verify the Adapter still satisfies the Port interface. |
| **Rationale** | Multi-implementation Port strategy (P10/P11) requires that all Adapters maintain compatible contracts. |

**Checklist**:
- [ ] Update `bom.yaml` version entry.
- [ ] Run `AuthPort` contract test against Keycloak Adapter.
- [ ] Run `CachePort` contract test against Redis AND Valkey Adapters.
- [ ] Run `MultiTenancyPort` contract test against Capsule Adapter (if applicable).

---

## RULE-08: Integration Point Initialization Order

| Aspect | Detail |
| --- | --- |
| **Trigger** | Service startup / readiness probe. |
| **Constraint** | The following must be available before marking readiness: PostgreSQL (base), Keycloak (Auth SPI), Redis (Cache SPI). Capsule (MultiTenancy SPI) is optional and only required if `multitenancy.enabled=true`. |
| **Rationale** | Ensures the service doesn't accept traffic before critical dependencies are reachable. |

**Dependency startup order**:
1. PostgreSQL → Keycloak → Redis
2. Capsule (conditional on multi-tenancy)

---

## RULE-09: Tenant Data Isolation Enforcement

| Aspect | Detail |
| --- | --- |
| **Trigger** | Any data access operation (read or write) in a multi-tenant context. |
| **Constraint** | Control-plane data uses `tenant_id` column-level isolation + RLS. Multi-tenancy lit up: business data follows Schema-per-tenant + RLS / Milvus Collection prefix / MinIO Bucket isolation (§8.2 matrix). This service itself does NOT hold vector/object data; only delivers isolation policies. |
| **Rationale** | Prevents cross-tenant data leakage at the persistence layer. |

**Implementation pattern**:
```java
// All repository queries MUST include tenant_id filter
@Query("SELECT t FROM TenantEntity t WHERE t.tenantId = :tenantId")
Optional<TenantEntity> findByTenantId(@Param("tenantId") String tenantId);
// RLS policy in PostgreSQL enforces this at DB level as defense-in-depth.
```

---

## RULE-10: RBAC Scope Separation

| Aspect | Detail |
| --- | --- |
| **Trigger** | Any API endpoint invocation. |
| **Constraint** | Four RBAC roles: `platform-admin` (platform-wide), `tenant-admin` (tenant-scoped), `developer` (app-level), `viewer` (read-only). Platform-level vs. tenant-level scope is STRICTLY separated. Admin operations require MFA (§14.6). |
| **Rationale** | Prevents tenant admins from accessing other tenants' data or platform-level configuration. |

**Implementation pattern**:
```java
@PreAuthorize("hasRole('platform-admin') or " +
    "(hasRole('tenant-admin') and #tenantId == authentication.tenantId)")
public TenantProfile getProfile(String tenantId) { ... }
```

---

## RULE-11: Immutable Audit Trail

| Aspect | Detail |
| --- | --- |
| **Trigger** | Any write operation (tenant/user/quota/app/entitlement change). |
| **Constraint** | All management-plane changes are recorded in `audit_log` (immutable, append-only). Audit happens even when `security` profile is disabled (§14.6). Implementation via `@Audit` annotation at application service layer. Audit events also flow into OTel. |
| **Rationale** | Compliance requirement; audit must not be tied to optional security profile. |

**Implementation pattern**:
```java
@Audit(action = "TENANT_CREATED")
public Tenant createTenant(CreateTenantCommand cmd) { ... }
// Aspect auto-captures: actor, tenant_id, action, payload, traceId → audit_log
```

---

> **References**: Full domain rules in `docs/DESIGN.md` §5, §11, §12. Also cross-reference `docs/SKILLS.md` across other repos for cross-service rules (e.g., `bump-spi-version` is shared).
