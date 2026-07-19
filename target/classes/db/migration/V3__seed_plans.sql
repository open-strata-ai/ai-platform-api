-- Seed the three predefined plan tiers (matches InMemoryPlanRepository defaults).
-- Insert is idempotent: ON CONFLICT DO NOTHING so re-runs are safe.
INSERT INTO plans (plan_id, name, quota_template) VALUES
  ('trial',     'Trial',     '{"cpu":1,"token":1000,"qps":10,"vector":0,"gpu":0}'),
  ('standard',  'Standard',  '{"cpu":4,"token":10000,"qps":50,"vector":1,"gpu":0}'),
  ('enterprise','Enterprise','{"cpu":16,"token":100000,"qps":200,"vector":10,"gpu":4}')
ON CONFLICT (plan_id) DO NOTHING;
