-- P2: platform-api-owned binding/whitelist tables for the real prod adapters.
-- Bindings (tool/skill), eval-run records, and the per-tenant model whitelist
-- are platform-api aggregates; the sibling services are only read-through targets.

CREATE TABLE agent_tool_bindings (
    agent_id VARCHAR(255) NOT NULL,
    tool_id  VARCHAR(255) NOT NULL,
    PRIMARY KEY (agent_id, tool_id)
);

CREATE TABLE agent_skill_bindings (
    agent_id VARCHAR(255) NOT NULL,
    skill_id VARCHAR(255) NOT NULL,
    PRIMARY KEY (agent_id, skill_id)
);

CREATE TABLE eval_runs (
    agent_id VARCHAR(255) NOT NULL,
    run_id   VARCHAR(255) NOT NULL,
    PRIMARY KEY (agent_id, run_id)
);

CREATE TABLE model_whitelist (
    tenant_id VARCHAR(255) NOT NULL,
    model     VARCHAR(255) NOT NULL,
    PRIMARY KEY (tenant_id, model)
);
