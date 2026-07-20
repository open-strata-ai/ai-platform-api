-- V5: RLS session-variable wiring support (R-002).
--
-- V2 enabled RLS and installed a placeholder policy that reads
-- current_setting('app.tenant_id'). That variable is only set per-transaction by
-- RlsTransactionManager (prod profile). This migration additionally lets a
-- platform-admin caller bypass tenant scoping via app.bypass_rls = 'on', which
-- the transaction manager sets for platform-admin actors.

ALTER POLICY tenant_isolation ON tenants
  USING (
    tenant_id = current_setting('app.tenant_id', true)
    OR current_setting('app.bypass_rls', 'off') = 'on'
  );
