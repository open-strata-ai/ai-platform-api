-- ai-platform-api initial schema (platform_cp). For production Flyway.
CREATE TABLE IF NOT EXISTS tenants (
  tenant_id     VARCHAR(64) PRIMARY KEY,
  name          VARCHAR(128) NOT NULL,
  status        VARCHAR(16)  NOT NULL DEFAULT 'PROVISIONING',
  plan_id       VARCHAR(32),
  multitenancy  BOOLEAN      NOT NULL DEFAULT FALSE,
  created_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
  updated_at    TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS users (
  user_id    VARCHAR(64) PRIMARY KEY,
  tenant_id  VARCHAR(64) NOT NULL REFERENCES tenants(tenant_id),
  email      VARCHAR(256) NOT NULL,
  role       VARCHAR(16)  NOT NULL,
  status     VARCHAR(16)  NOT NULL DEFAULT 'INVITED',
  kc_id      VARCHAR(64),
  UNIQUE (tenant_id, email)
);

CREATE TABLE IF NOT EXISTS applications (
  app_id        VARCHAR(64) PRIMARY KEY,
  tenant_id     VARCHAR(64) NOT NULL REFERENCES tenants(tenant_id),
  name          VARCHAR(128) NOT NULL,
  agentspec_ref VARCHAR(256)
);

CREATE TABLE IF NOT EXISTS plans (
  plan_id        VARCHAR(32) PRIMARY KEY,
  name           VARCHAR(64) NOT NULL,
  quota_template TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS quotas (
  quota_id   VARCHAR(64) PRIMARY KEY,
  tenant_id  VARCHAR(64) NOT NULL REFERENCES tenants(tenant_id),
  dimension  VARCHAR(16) NOT NULL,
  limit_val  BIGINT NOT NULL,
  used_val   BIGINT NOT NULL DEFAULT 0,
  UNIQUE (tenant_id, dimension)
);

CREATE TABLE IF NOT EXISTS entitlements (
  tenant_id  VARCHAR(64) NOT NULL REFERENCES tenants(tenant_id),
  component  VARCHAR(48) NOT NULL,
  allowed    BOOLEAN NOT NULL DEFAULT FALSE,
  PRIMARY KEY (tenant_id, component)
);

CREATE TABLE IF NOT EXISTS model_grants (
  tenant_id VARCHAR(64) NOT NULL REFERENCES tenants(tenant_id),
  provider  VARCHAR(32) NOT NULL,
  model     VARCHAR(64) NOT NULL,
  PRIMARY KEY (tenant_id, provider, model)
);

CREATE TABLE IF NOT EXISTS audit_log (
  id          BIGSERIAL PRIMARY KEY,
  tenant_id   VARCHAR(64),
  actor       VARCHAR(64) NOT NULL,
  action      VARCHAR(64) NOT NULL,
  payload     TEXT,
  created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);
