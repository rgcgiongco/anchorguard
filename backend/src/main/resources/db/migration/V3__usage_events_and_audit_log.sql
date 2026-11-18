-- V3: usage_events evolution and audit_log (additive)

-- usage_events: add spec-aligned session/profile fields while retaining V1 columns
ALTER TABLE usage_events ADD COLUMN IF NOT EXISTS device_user_id UUID REFERENCES device_users(id) ON DELETE SET NULL;
ALTER TABLE usage_events ADD COLUMN IF NOT EXISTS started_at TIMESTAMPTZ;
ALTER TABLE usage_events ADD COLUMN IF NOT EXISTS ended_at TIMESTAMPTZ;
ALTER TABLE usage_events ADD COLUMN IF NOT EXISTS duration_ms BIGINT;

-- backfill started_at from legacy occurred_at for existing rows
UPDATE usage_events SET started_at = occurred_at WHERE started_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_usage_events_device_user_started ON usage_events(device_user_id, started_at);

-- audit_log: enforcement/audit events
CREATE TABLE IF NOT EXISTS audit_log (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  device_id UUID REFERENCES devices(id) ON DELETE CASCADE,
  actor TEXT NOT NULL,
  action TEXT NOT NULL,
  target_package TEXT,
  occurred_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  details_json JSONB
);

CREATE INDEX IF NOT EXISTS idx_audit_log_device_occurred ON audit_log(device_id, occurred_at DESC);
CREATE INDEX IF NOT EXISTS idx_audit_log_occurred ON audit_log(occurred_at DESC);
