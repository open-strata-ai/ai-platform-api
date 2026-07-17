# ai-platform-api · Architecture Decision Document (ARCH)

> **Source**: Extracted from `design/DESIGN.md` §1, §2, §3, §6. Full design doc is the authority; this distillate captures architectural decisions, constraints, and SPI boundaries for implementers.

---

## 1. Service Identity

| Attribute | Value |
| --- | --- |
| Domain | control-plane (平台控制面核心) |
| Language / Framework | Java · Spring Boot 3.x (Jakarta Persistence / MyBatis-Flex) |
| Optional | No — core, enabled in all profiles (starter~full) |
| Default Port | 8081 |
| Platform Version | v1.4.0 |
| Deployment | 2-3 replicas, `ai-system` namespace, 500m CPU / 1Gi request |
| Database | PostgreSQL@16.0 (core base), schema `platform_cp` |

---

## 2. Bounded Context

`ai-platform-api` is the **platform control-plane core API**. It is the authoritative domain model and write-path for:
- Tenant lifecycle (create/configure/suspend/deregister)
- User identity and role binding (CRUD, invite/disable, Keycloak mapping)
- Application (Agent App) registration (model/tool binding, `AgentSpec` metadata)
- Plan and quota templates (Trial/Standard/Enterprise with CPU/Token/QPS/Vector limits)
- Multi-tenancy enablement (Capsule Tenant + Namespace + NetworkPolicy + data prefix)
- Entitlement (component whitelist) and model grants (vendor/model authorization)

### 2.1 Profile-Gated Behavior

| Profile | multitenancy | billing | security | multiTenancyProvider |
| --- | --- | --- | --- | --- |
| starter | false | false | false | none (null adapter) |
| standard | false | false | false | none (null adapter) |
| advanced | true | true | false | Capsule |
| full | true | true | true (ai-srs-service) | Capsule |

### 2.2 Upstream Consumers

| Consumer | Port | Auth Type | Role |
| --- | --- | --- | --- |
| `ai-portal-frontend` | REST 8081 | Bearer JWT + X-Tenant-Id | User-facing agent/app management |
| `ai-admin-frontend` | REST 8081 | Bearer JWT + X-Tenant-Id | Admin governance UI |
| `ai-sdk-java` / `aictl` CLI | REST 8081 | Bearer JWT + X-Tenant-Id | Programmatic/SDK access |

### 2.3 Downstream Dependencies (via SPI/ACL)

| Dependency | Call Pattern | Relationship |
| --- | --- | --- |
| `ai-admin-service` | REST (ControlPlaneClient, consumed by admin) | Governance orchestrator; consumes this service's domain authority |
| `ai-gateway-core` | REST | Quota/route binding; receives quota push and app registration |
| `ai-billing-service` | Event/REST | Billing linkage; tenant/entitlement change events |
| `ai-srs-service` | REST (optional, full profile) | Plan/Rule reference for approval policies |
| `ai-dependency-resolver` (Go) | REST | Manifest dependency resolution and delivery |

### 2.4 Boundary Rules (Explicit Constraints)
- **Inbound**: Only accepts requests authenticated via `Auth` SPI (Keycloak OIDC/JWT) with gateway-injected `X-Tenant-Id`. No anonymous access.
- **Out-of-scope (NEVER in this service)**: Model inference, RAG/memory, metering collection, gateway routing, settlement billing, Agent runtime execution.
- **Outbound (ALWAYS via SPI Ports)**: All external system calls go through domain-layer Port interfaces with infrastructure-layer Adapter + ACL. Direct external calls from domain or application layers are FORBIDDEN.
- **Data Authority**: This service is the SINGLE SOURCE OF TRUTH for tenant/user/app/plan/quota/entitlement/model-grant data. All writes to these aggregates happen here. `ai-admin-service` calls this service for writes; never writes directly.
- **Immutable Audit**: All management-plane changes write to `audit_log` regardless of profile. Audit is a core, non-optional capability.

### 2.5 Relationship with ai-admin-service (Critical Distinction)

This is the **domain authority** (data + rules). `ai-admin-service` is the **governance orchestrator** — dispatches admin intent across multiple managed planes. Admin-service calls this service via `ControlPlaneClient` SPI (ACL-encapsulated). Admin-service NEVER writes directly to this service's PostgreSQL database. This prevents dual-write conflicts on tenant/user/quota data.

---

## 3. Responsibility Matrix

### 3.1 Core Capabilities

| Capability | Description | Arch § | Critical Rules |
| --- | --- | --- | --- |
| Tenant Lifecycle | Create/configure/suspend/delete. `tenant_id` globally unique, spans all resources. | §4.7.1 / §8.1 | ID uniqueness enforced by DB constraint + domain validation |
| User & Identity Binding | User CRUD, invite/disable, Keycloak account mapping (`kc_id` column). | §4.7.3 / §14.3 | Email unique per tenant; Keycloak sync via Auth SPI |
| Application Management | App registration, model/tool binding, `AgentSpec` metadata write. | §4.3.5 | AgentSpecRef format: `apiVersion/kind/metadata.name` |
| Plan & Quota Templates | Trial/Standard/Enterprise presets. CPU/Token/QPS/Vector quotas. | §8.1 / §14.2 | GPU quota only effective for full profile self-hosted inference |
| Multi-tenancy Enablement | `multitenancy.enabled=true` → Capsule Tenant + Namespace + NetworkPolicy + data prefix. | §8.2 / §12.1 | Requires `auth` precondition (§12.4) |
| RBAC & Approval | platform-admin/tenant-admin/developer/viewer roles + high-risk operation approval flow. | §4.7.3 / §14.3 | Platform vs tenant scope STRICTLY separated |
| Component Scope Delivery | Tenant-level component whitelist → `PlatformManifest.spec` write. | §12 / §14.2 | Must satisfy §12.4 dependency graph |
| Vendor Authorization | Per-tenant third-party model grants. | §14.2 | Enterprise-only for restricted models |
| Audit Trail | All changes audited in immutable `audit_log`. | §4.7.4 / §14.6 | Core, not optional; active even when security=off |

### 3.2 Application Layer Use Cases (from §4)

Each use case maps to a single Application Service with `@Transactional` boundary and domain event emission.

| Use Case | App Service | TX Type | Domain Event | CQRS |
| --- | --- | --- | --- | --- |
| Create tenant | `TenantAppService.createTenant()` | Write | `TenantCreated` | Command |
| Suspend/resume/delete tenant | `TenantAppService.suspend/resume/delete()` | Write | `TenantSuspended/Deleted` | Command |
| Enable multi-tenancy | `TenantAppService.enableMultitenancy()` | Write | `TenantMultitenancyEnabled` | Command |
| Invite/disable user | `UserAppService.invite/disable()` | Write | `UserInvited/Disabled` | Command |
| Change user role | `UserAppService.changeRole()` | Write | `UserRoleChanged` | Command |
| Register/unregister app | `ApplicationAppService.register/unregister()` | Write | `ApplicationRegistered/Unregistered` | Command |
| Assign plan | `PlanAppService.assignPlan()` | Write | `PlanChanged → QuotaPolicyUpdated` | Command |
| Adjust quota | `QuotaAppService.updateQuota()` | Write | `QuotaPolicyUpdated` | Command |
| Set component whitelist | `EntitlementAppService.setEntitlements()` | Write | `EntitlementChanged` | Command |
| Grant model vendor | `ModelGrantAppService.grant()` | Write | `EntitlementChanged` | Command |
| Query tenant resource profile | `TenantQueryService.getProfile()` | Read | — (Read Projection) | Query |
| Submit/approve high-risk op | `ApprovalAppService.submit/approve()` | Write | `ApprovalDecided` | Command |

### 3.3 Explicit Exclusions

| Responsibility | Handled By |
| --- | --- |
| Gateway routing / metering collection | `ai-gateway-core` (Go) |
| Settlement / billing / invoicing | `ai-billing-service` (Java, multi-tenant only) |
| RAG / memory / vector search | `memory/rag` (data-plane services) |
| Agent runtime execution (inference, tool calls) | Agent engine (AgentRuntime) |
| Skills/Rules/Specs storage and validation | `ai-srs-service` (Java, optional) |
| Governance orchestration / multi-target dispatch | `ai-admin-service` (Java) |

---

## 4. Domain Model

### 4.1 Architecture Style

Follows **DDD four-layer architecture** (§15.6.2):
- **Interface Layer (①)**: REST controllers, DTO mapping, `@PreAuthorize` annotations.
- **Application Layer (②)**: Application Services with `@Transactional`, DTO↔Domain conversion, `@Audit` aspect.
- **Domain Layer (③)**: Aggregates, Entities, Value Objects, Domain Services, Domain Events, **Port interfaces only** (zero external dependencies).
- **Infrastructure Layer (④)**: Adapter implementations + Anti-Corruption Layers, JPA repositories, Flyway migrations.

**CQRS**: Write path uses Command services with strong consistency. Read path uses Query services with Projections (read-optimized, may be cached).

### 4.2 Aggregate Design

| Aggregate Root | Consistency Boundary | Tenant Relationship | Lifecycle |
| --- | --- | --- | --- |
| `Tenant` | Users, Applications, Plan, Quotas, Entitlements, ModelGrants — all within a single tenant. Multi-tenancy enablement is an intra-aggregate behavior. | — (root) | PROVISIONING → ACTIVE → SUSPENDED → DELETED |
| `Application` | Independent app registration and lifecycle. References `AgentSpecRef` but does not own it. | Belongs to one Tenant | Registered → Active → Unregistered |

**Tenant Aggregate Internal Structure**:
```
Tenant (root)
 ├── User[1..*]          — email, role, status, kc_id
 ├── Application[1..*]   — name, AgentSpecRef
 ├── Plan[0..1]          — planId, quotaTemplate
 ├── Quota[1..*]         — dimension, limit, used
 ├── Entitlement[1..*]   — component, allowed (boolean)
 └── ModelGrant[1..*]    — provider, model
```

### 4.3 Entities (Identity-Based)

| Entity | Identity Field | Key Attributes |
| --- | --- | --- |
| `User` | `UserId` | email, tenantId, role (enum), status, kc_id |
| `Plan` | `PlanId` | name, quotaTemplate (JSONB) |
| `Quota` | `QuotaId` | tenantId, dimension (enum), limit, used |
| `Entitlement` | composite (tenantId+component) | component, allowed |
| `ModelGrant` | composite (tenantId+provider+model) | provider, model |
| `AgentSpecRef` | value object | apiVersion, kind, metadata.name |

### 4.4 Value Objects (Immutable, No Identity)

| VO | Type | Constraints / Enum Values |
| --- | --- | --- |
| `TenantId`, `UserId`, `AppId`, `PlanId`, `QuotaId` | String | UUID format, globally unique |
| `TenantStatus` | Enum | `PROVISIONING`, `ACTIVE`, `SUSPENDED`, `DELETED` |
| `Role` | Enum | `platform-admin` (platform-wide), `tenant-admin` (tenant-scoped), `developer` (app-level), `viewer` (read-only) |
| `ResourceDimension` | Enum | `CPU`, `TOKEN`, `QPS`, `VECTOR`, `GPU` |
| `QuotaTemplate` | JSON | `{cpu, token, qps, vector, gpu}` per plan tier |
| `UserStatus` | Enum | `INVITED`, `ACTIVE`, `DISABLED` |

### 4.5 Domain Events (Async Delivery via ApplicationEvent / Event Table)

Events are published AFTER the aggregate transaction commits. Side effects execute asynchronously via SPI adapters. The domain transaction itself is the atomic boundary; side effects are eventually consistent.

| Event | Aggregate | Trigger | Consumer SPI Adapters | Side Effects |
| --- | --- | --- | --- | --- |
| `TenantCreated` | Tenant | New tenant aggregate saved | AuthPort, MultiTenancyPort | Create Keycloak realm; optionally create Capsule Tenant |
| `TenantMultitenancyEnabled` | Tenant | `enableMultitenancy()` called | MultiTenancyPort, ManifestPort | Create Namespace+NetworkPolicy+ResourceQuota; push Manifest |
| `PlanChanged` | Tenant | Plan assigned/changed | (internal) QuotaPolicyService | Recompute quota limits; publish `QuotaPolicyUpdated` |
| `QuotaPolicyUpdated` | Tenant | After plan change or quota adjustment | AppRegistryPort, BillingEventPort | Push quotas to gateway; notify billing of entitlement change |
| `EntitlementChanged` | Tenant | Component whitelist changed | ManifestPort | Rewrite tenant `PlatformManifest.spec` |
| `UserRoleChanged` | User | Role assigned/changed | AuthPort | Sync Keycloak roles and client mappings |
| `UserInvited` | User | New user invited | AuthPort | Create/update Keycloak user account |
| `ApplicationRegistered` | Application | New app registered | AppRegistryPort | Broadcast app to gateway/tool registry |

### 4.6 Domain Services (Pure Logic, No Side Effects)

These services are defined in the domain layer, operate only on domain objects, and are independently unit-testable:

| Domain Service | Responsibility | Key Rule |
| --- | --- | --- |
| `QuotaPolicyService` | Compute per-dimension limits from `Plan.quotaTemplate` | GPU quota only active when `modelServing.enabled=true` (full profile) |
| `MultitenancyProvisioningRule` | Validate preconditions for multi-tenancy enablement | `multitenancy.enabled=true` AND `auth` is active (§12.4 dependency) |
| `EntitlementConsistencyRule` | Validate component whitelist against Manifest dependency graph | `billing` requires `multitenancy`; `multitenancy` requires `auth` |
| `ModelGrantRule` | Validate model authorization against tenant plan | Enterprise-only for restricted models |
| `TenantIdUniquenessRule` | Ensure global uniqueness of tenant_id | DB unique constraint + domain-level pre-check |
| `ApprovalRule` | Validate high-risk operations have approval | Rules loaded from `ai-srs-service` via `PolicyRulePort` |

---

## 5. SPI Ports & Adapters

### 5.1 Architecture Principle

Domain layer (③) defines **Port interfaces ONLY** — pure Java interfaces, no implementation, no external dependencies. Infrastructure layer (④) implements **Adapter + Anti-Corruption Layer (ACL)**.

The ACL's job is to translate between external system DTOs/APIs and internal domain objects. This keeps the domain layer pure and allows switching implementations with zero domain code changes (§10.4, §15.6.4).

```
┌──────────────┐     ┌─────────────────────┐     ┌─────────────────┐
│ Domain Layer │────►│ Infrastructure Layer │────►│ External System  │
│ Port (interface)  │ Adapter + ACL         │     │ (Keycloak, etc.) │
└──────────────┘     └─────────────────────┘     └─────────────────┘
     ▲                        ▲                         ▲
     │ Pure Java,             │ Translates external     │ Unmodified
     │ zero deps              │ format to domain model  │ third-party
```

### 5.2 Port Inventory

| Port (domain interface) | SPI Port (bom.yaml) | Default Adapter (core) | Alternative Adapter (optional) | ACL Responsibility |
| --- | --- | --- | --- | --- |
| `AuthPort` | `Auth` (§4.7.3) | **Keycloak@25.0.0** ✅ | — | External OIDC/JWT token + claims → internal `TenantContext`, `Role` enum |
| `MultiTenancyPort` | `MultiTenancy` (§8.2) | **Capsule@1.9.0** ✅ | null-object (single-tenant) | Tenant CRD (`apiVersion: capsule.clastix.io/v1beta2`) ⇄ internal `IsolationSpec` |
| `CachePort` | `Cache` (§4.3.4) | **Redis@7.4.0** ✅ | **Valkey@7.2.0** (optional OSI) | Tenant-level key prefix isolation (`{tenantId}:cache_key`); serialization format |
| `ManifestPort` | — (config-driven, §12) | Local YAML/PostgreSQL + `ai-dependency-resolver` (Go) delivery | — | Manifest DTO (`PlatformManifest.spec`) ⇄ internal `TenantConfig` |
| `AppRegistryPort` | — | REST → `ai-gateway-core` / `ai-tool-registry` | — | Internal `Application` + `Quota` ⇄ gateway schema and tool registry format |
| `PolicyRulePort` | — (§7.3) | REST → `ai-srs-service` (Rules API) | — | Rego/Drools policy expression ⇄ internal `ApprovalRule` domain object |
| `BillingEventPort` | — (§8.3) | Event/REST → `ai-billing-service` | — | Tenant/quota change event ⇄ billing metering enablement command |
| `ControlPlaneClient` | — | REST (consumed BY `ai-admin-service`) | — | Internal domain objects (`Tenant`, `Quota`, etc.) ⇄ admin governance DTOs |

### 5.3 Port Interaction Patterns

**Synchronous Request-Response** (used for Auth, Cache):
```java
// Domain layer calls Port interface; infrastructure Adapter handles the call
TenantContext ctx = authPort.validate(token, tenantId);
```

**Event-Driven (used for domain event side effects)**:
```java
// After TenantCreated event:
// 1. AuthPort → create Keycloak realm (async)
// 2. MultiTenancyPort → create Capsule Tenant (async, if multitenancy=true)
```

**Request-Response with ACL Translation** (used for Manifest, AppRegistry, PolicyRule, BillingEvent, ControlPlaneClient):
```java
// Infrastructure ACL translates domain objects to/from external DTOs
ManifestDto dto = manifestAcl.toDto(tenantConfig);
dependencyResolver.pushManifest(dto);
```

**Null-Object Pattern** (used when capability is disabled):
```java
// When multitenancy.enabled=false, MultiTenancyPort has a null-object implementation
// that does nothing — no Capsule calls, no errors. P10: 0 adapters = skipped.
```

### 5.4 Multi-Implementation Coexistence (P10/P11 Patterns)

| Scenario | Port | Implementation Strategy |
| --- | --- | --- |
| Primary + Alternative (P10) | `CachePort` | Redis (default, core) + Valkey (optional, OSI compliance). Both registered as Spring beans; active one selected by `openstrata.spi.cache.provider` config. Domain code calls `CachePort` interface and never knows which is active. |
| Capability Skip (P10: 0 adapters) | `MultiTenancyPort` | When `multitenancy.enabled=false`, a NoOpMultiTenancyAdapter is registered that implements the interface but performs zero operations. Domain code calls `multiTenancyPort.createTenant()` and gets a no-op response. |
| Event Fan-Out | `AppRegistryPort` | Single Port interface; Adapter fans out to both `ai-gateway-core` (quota push) and `ai-tool-registry` (app registration). Domain code publishes `ApplicationRegistered` event; infrastructure handles multi-target fan-out. |

### 5.5 Dependency Initialization Order

Readiness probe requires all core dependencies to be available:

```
1. PostgreSQL (base, data persistence)          ← REQUIRED for all profiles
2. Keycloak (AuthPort, authentication)          ← REQUIRED for all profiles
3. Redis (CachePort, caching)                   ← REQUIRED for all profiles
4. Capsule (MultiTenancyPort)                   ← CONDITIONAL: only advanced/full
```

If Capsule is unavailable when `multitenancy.enabled=true`, readiness fails. If `multitenancy.enabled=false`, Capsule is not checked.

### 5.6 External Dependencies (bom.yaml alignment)

| Integration Point | Type | Version | License | Scope | Port |
| --- | --- | --- | --- | --- | --- |
| Keycloak | External OSS | 25.0.0 | Apache-2.0 | core | AuthPort |
| Capsule | External OSS | 1.9.0 | Apache-2.0 | optional (multi-tenant) | MultiTenancyPort |
| Redis | External OSS | 7.4.0 | BSD-3 | core | CachePort |
| Valkey | External OSS | 7.2.0 | BSD-3 | optional (OSI alternative) | CachePort |
| PostgreSQL | External OSS | 16.0 | PostgreSQL | core base | — (direct JPA) |
| ai-dependency-resolver | Internal (Go) | v1.4.0 | internal | core | ManifestPort |
| ai-admin-service | Internal (Java) | v1.4.0 | internal | core | ControlPlaneClient |
| ai-gateway-core | Internal (Go) | v1.4.0 | internal | core | AppRegistryPort |
| ai-billing-service | Internal (Java) | v1.4.0 | internal | multi-tenant only | BillingEventPort |
| ai-srs-service | Internal (Java) | v1.4.0 | internal | optional (full) | PolicyRulePort |
| ai-tool-registry | Internal (Go) | v1.4.0 | internal | core | AppRegistryPort |

---

## 6. Key Architectural Decisions

| # | Decision | Rationale | Impact |
| --- | --- | --- | --- |
| ADR-1 | `Tenant` as single aggregate root for user/app/plan/quota/entitlement/model-grant | Ensures consistency boundary for multi-tenancy enablement, plan changes, and entitlement updates. All sub-entities change within a single transaction. | Cannot modify User independently of Tenant; must go through Tenant aggregate. |
| ADR-2 | CQRS: write Commands, read Queries (Projections) | Separates transactional writes from read-side projections; read paths can cache freely without affecting write consistency. | Two code paths for write/read; need to keep Projections in sync. |
| ADR-3 | All external calls through SPI Ports + ACL | Domain layer stays pure; infrastructure changes (e.g., Redis→Valkey) require zero domain code changes. | Added indirection; ACL must be maintained when external schemas change. |
| ADR-4 | `ControlPlaneClient` SPI for admin-service consumption | Prevents `ai-admin-service` from directly writing platform-api's database; ensures single authoritative write path for tenant/user data. | Admin-service must call this service for all writes; adds network hop. |
| ADR-5 | Multi-tenancy as intra-aggregate domain behavior on `Tenant` | Lights up Capsule/K8s/Keycloak as a coordinated atomic step, not scattered orchestration. | Tenant aggregate holds `multitenancyEnabled` flag; all multi-tenant provisioning flows from it. |
| ADR-6 | Event-driven side effects (Domain Events → SPI adapters) | Decouples the core domain transaction from external provisioning (Capsule, Keycloak, Manifest). Domain transaction commits; events fire asynchronously. | Eventual consistency for external side effects; need retry/compensation for failures. |
| ADR-7 | Immutable `audit_log` at application layer via `@Audit` aspect | Guarantees complete audit trail regardless of `security` profile; OTel trace correlation for cross-service audit. | All write paths must be annotated with `@Audit`; aspect must not fail the transaction. |
| ADR-8 | PostgreSQL with `tenant_id` column isolation + RLS | Column-level tenant isolation for control-plane data; RLS as defense-in-depth. Schema-per-tenant after multi-tenancy lit for business data. | All repository queries must include `tenant_id` filter; RLS policies must be tested. |

---

## 7. Service Boundary Diagram

```text
┌──────────────────────────────────────────────────────────────┐
│                    Upstream Consumers                         │
│  ai-portal-frontend │ ai-admin-frontend │ ai-sdk-java/aictl  │
└────────────────────────────┬─────────────────────────────────┘
                             │ Auth'd, X-Tenant-Id header
┌────────────────────────────▼─────────────────────────────────┐
│                   ai-platform-api (8081)                      │
│                                                               │
│  ┌────────────────── Application Layer ──────────────────┐   │
│  │ TenantAppService        UserAppService                 │   │
│  │ ApplicationAppService   PlanAppService                 │   │
│  │ QuotaAppService         EntitlementAppService          │   │
│  │ ModelGrantAppService    ApprovalAppService             │   │
│  │ TenantQueryService (CQRS Read Projection)              │   │
│  │ @Transactional + @Audit aspect + DTO↔Domain mapping    │   │
│  └────────────────────────────────────────────────────────┘   │
│  ┌────────────────── Domain Layer (3) ────────────────────┐   │
│  │ Aggregates: Tenant (root), Application (root)          │   │
│  │ Entities: User, Plan, Quota, Entitlement, ModelGrant   │   │
│  │ Value Objects: TenantId, Role, ResourceDimension, ...  │   │
│  │ Domain Services: QuotaPolicyService, EntitlementRule,  │   │
│  │   MultitenancyProvisioningRule, ModelGrantRule, ...    │   │
│  │ Domain Events: TenantCreated, PlanChanged, ...         │   │
│  │                                                         │   │
│  │ Port Interfaces (pure Java, 8 ports):                   │   │
│  │ AuthPort │ MultiTenancyPort │ CachePort                 │   │
│  │ ManifestPort │ AppRegistryPort │ PolicyRulePort         │   │
│  │ BillingEventPort │ ControlPlaneClient                   │   │
│  └────────────────────────────────────────────────────────┘   │
│  ┌──────────────── Infrastructure Layer (4) ──────────────┐   │
│  │ Adapters:                                               │   │
│  │ KeycloakAdapter (Auth) │ CapsuleAdapter (MultiTenancy)  │   │
│  │ RedisAdapter (Cache)   │ ValkeyAdapter (Cache alt)      │   │
│  │ ManifestAdapter        │ AppRegistryAdapter              │   │
│  │ PolicyRuleAdapter      │ BillingEventAdapter             │   │
│  │                                                         │   │
│  │ ACL Functions: External format ⇄ Domain object mapping  │   │
│  │ JPA Repositories: TenantEntity → tenants table, etc.    │   │
│  │ Flyway Migrations: V1__init.sql, V2__rls.sql            │   │
│  └────────────────────────────────────────────────────────┘   │
└────────────────────────────┬─────────────────────────────────┘
                             │ SPI/ACL calls (REST, Events)
┌────────────────────────────▼─────────────────────────────────┐
│                  Downstream Dependencies                       │
│  Keycloak (Auth)    │ Capsule (MultiTenancy)                   │
│  Redis/Valkey (Cache)│ PostgreSQL (Base)                      │
│  ai-admin-service   │ ai-gateway-core                         │
│  ai-billing-service │ ai-srs-service                          │
│  ai-dependency-resolver │ ai-tool-registry                    │
└──────────────────────────────────────────────────────────────┘
```

---

> **References**:
> - Full design: `design/DESIGN.md` (16 sections, all architectural decisions)
> - Architecture framework: `../../OpenStrata架构设计文档 v2.8.md` §4.1, §8, §12, §14, §15.6, §16
> - SPI contract tests: `skills/SKILLS.md` — `bump-spi-version` rule
> - OpenAPI spec: `specs/SPECS.md` — endpoint table and data model DDL
