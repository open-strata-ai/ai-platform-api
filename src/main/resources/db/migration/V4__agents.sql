-- Agent control plane schema (DV-01..05, DV-11, DV-15). Backs AgentRepository in
-- production; the offline profile uses the in-memory adapter. AgentSpec is a YAML
-- runtime contract stored as TEXT (DESIGN §4.3.5), not a typed relational model.
CREATE TABLE IF NOT EXISTS agents (
  agent_id    VARCHAR(64) PRIMARY KEY,
  tenant_id   VARCHAR(64) NOT NULL REFERENCES tenants(tenant_id),
  created_by  VARCHAR(64) NOT NULL,
  name        VARCHAR(128) NOT NULL,
  status      VARCHAR(16)  NOT NULL DEFAULT 'DRAFT',
  spec        TEXT
);
CREATE INDEX IF NOT EXISTS idx_agents_tenant ON agents(tenant_id);

CREATE TABLE IF NOT EXISTS agent_versions (
  version_id     VARCHAR(64) PRIMARY KEY,
  agent_id       VARCHAR(64) NOT NULL REFERENCES agents(agent_id),
  version        VARCHAR(32) NOT NULL,
  status         VARCHAR(16) NOT NULL DEFAULT 'DRAFT',
  spec_snapshot  TEXT,
  created_at     BIGINT      NOT NULL,
  UNIQUE (agent_id, version)
);
CREATE INDEX IF NOT EXISTS idx_agent_versions_agent ON agent_versions(agent_id);

ALTER TABLE agents         ENABLE ROW LEVEL SECURITY;
ALTER TABLE agent_versions ENABLE ROW LEVEL SECURITY;
