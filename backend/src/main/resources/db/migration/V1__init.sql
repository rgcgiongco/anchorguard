-- parents
CREATE TABLE parents (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  email TEXT UNIQUE NOT NULL,
  password_hash TEXT NOT NULL,
  name TEXT,
  email_verified BOOLEAN DEFAULT FALSE,
  created_at TIMESTAMPTZ DEFAULT NOW(),
  updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- devices
CREATE TABLE devices (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  hex_id TEXT UNIQUE NOT NULL,
  parent_id UUID REFERENCES parents(id),
  name TEXT,
  model TEXT,
  manufacturer TEXT,
  android_id TEXT,
  app_version TEXT,
  api_key_hash TEXT,
  last_seen TIMESTAMPTZ,
  is_online BOOLEAN DEFAULT FALSE,
  pairing_code TEXT,
  pairing_expires_at TIMESTAMPTZ,
  created_at TIMESTAMPTZ DEFAULT NOW()
);

-- app_policies
CREATE TABLE app_policies (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  device_id UUID REFERENCES devices(id) ON DELETE CASCADE,
  package_name TEXT NOT NULL,
  app_name TEXT,
  is_allowed BOOLEAN DEFAULT TRUE,
  daily_limit_minutes INTEGER,
  block_schedule JSONB,
  internet_access TEXT DEFAULT 'on',
  created_at TIMESTAMPTZ DEFAULT NOW(),
  updated_at TIMESTAMPTZ DEFAULT NOW(),
  UNIQUE (device_id, package_name)
);

-- device_settings
CREATE TABLE device_settings (
  device_id UUID PRIMARY KEY REFERENCES devices(id) ON DELETE CASCADE,
  advanced_timer BOOLEAN DEFAULT FALSE,
  background_mode BOOLEAN DEFAULT FALSE,
  tamper_proof BOOLEAN DEFAULT FALSE,
  boot_start BOOLEAN DEFAULT FALSE,
  keep_alive BOOLEAN DEFAULT FALSE,
  brute_force_pin BOOLEAN DEFAULT FALSE,
  block_interval_ms INTEGER DEFAULT 500,
  app_title TEXT DEFAULT 'AnchorGuard',
  weekly_schedule JSONB,
  updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- usage_events
CREATE TABLE usage_events (
  id BIGSERIAL PRIMARY KEY,
  device_id UUID REFERENCES devices(id) ON DELETE CASCADE,
  package_name TEXT NOT NULL,
  event_type TEXT NOT NULL,
  occurred_at TIMESTAMPTZ DEFAULT NOW()
);

-- device_users (profiles on device)
CREATE TABLE device_users (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  device_id UUID REFERENCES devices(id) ON DELETE CASCADE,
  name TEXT NOT NULL,
  pin_hash TEXT,
  created_at TIMESTAMPTZ DEFAULT NOW()
);

-- refresh tokens for rotation/logout (supports spec: invalidate refresh token)
CREATE TABLE refresh_tokens (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  parent_id UUID NOT NULL REFERENCES parents(id) ON DELETE CASCADE,
  token_hash TEXT NOT NULL UNIQUE,
  expires_at TIMESTAMPTZ NOT NULL,
  revoked_at TIMESTAMPTZ,
  created_at TIMESTAMPTZ DEFAULT NOW()
);

-- password reset tokens (supports spec: request/confirm reset)
CREATE TABLE password_reset_tokens (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  parent_id UUID NOT NULL REFERENCES parents(id) ON DELETE CASCADE,
  token_hash TEXT NOT NULL UNIQUE,
  expires_at TIMESTAMPTZ NOT NULL,
  used_at TIMESTAMPTZ,
  created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_devices_parent_id ON devices(parent_id);
CREATE INDEX idx_devices_pairing_code ON devices(pairing_code);
CREATE INDEX idx_app_policies_device_id ON app_policies(device_id);
CREATE INDEX idx_usage_events_device_occurred ON usage_events(device_id, occurred_at);
CREATE INDEX idx_device_users_device_id ON device_users(device_id);
CREATE INDEX idx_refresh_tokens_parent_id ON refresh_tokens(parent_id);
