-- Row-Level Security policy (R-002: RLS-primary, schema-per-tenant opt-in).
-- All tenant data is isolated by tenant_id; RLS enforces it at the DB tier as
-- defense-in-depth. The session tenant_id is set from X-Tenant-Id / Keycloak claim.

ALTER TABLE tenants       ENABLE ROW LEVEL SECURITY;
ALTER TABLE users         ENABLE ROW LEVEL SECURITY;
ALTER TABLE applications  ENABLE ROW LEVEL SECURITY;
ALTER TABLE quotas        ENABLE ROW LEVEL SECURITY;
ALTER TABLE entitlements  ENABLE ROW LEVEL SECURITY;
ALTER TABLE model_grants  ENABLE ROW LEVEL SECURITY;
ALTER TABLE audit_log     ENABLE ROW LEVEL SECURITY;

-- Placeholder policy: denies all by default until the session variable is set.
-- Production wires a tenant-aware policy using current_setting('app.tenant_id').
CREATE POLICY tenant_isolation ON tenants
  USING (tenant_id = current_setting('app.tenant_id', true));
