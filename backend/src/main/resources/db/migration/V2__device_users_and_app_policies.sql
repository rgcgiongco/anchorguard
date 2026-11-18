-- V2: device_users and app_policies alignment (additive, preserves V1/MVP behavior)

-- device_users: add spec-aligned profile columns
ALTER TABLE device_users ADD COLUMN IF NOT EXISTS child_name TEXT;
ALTER TABLE device_users ADD COLUMN IF NOT EXISTS age_birthday DATE;
ALTER TABLE device_users ADD COLUMN IF NOT EXISTS avatar_color TEXT;
ALTER TABLE device_users ADD COLUMN IF NOT EXISTS is_primary BOOLEAN DEFAULT FALSE;

-- backfill child_name from legacy name column where available
UPDATE device_users SET child_name = name WHERE child_name IS NULL AND name IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_device_users_device_primary ON device_users(device_id, is_primary);

-- app_policies: add per-user/scheduling/category fields (additive alongside device-scoped V1 columns)
ALTER TABLE app_policies ADD COLUMN IF NOT EXISTS device_user_id UUID REFERENCES device_users(id) ON DELETE CASCADE;
ALTER TABLE app_policies ADD COLUMN IF NOT EXISTS category TEXT;
ALTER TABLE app_policies ADD COLUMN IF NOT EXISTS blocked BOOLEAN;
ALTER TABLE app_policies ADD COLUMN IF NOT EXISTS schedule_start TEXT;
ALTER TABLE app_policies ADD COLUMN IF NOT EXISTS schedule_end TEXT;
ALTER TABLE app_policies ADD COLUMN IF NOT EXISTS weekdays_mask INTEGER;
ALTER TABLE app_policies ADD COLUMN IF NOT EXISTS blocked_message TEXT;

-- keep compatibility: derive blocked from legacy is_allowed when new column is null
UPDATE app_policies SET blocked = (NOT COALESCE(is_allowed, TRUE)) WHERE blocked IS NULL;

CREATE INDEX IF NOT EXISTS idx_app_policies_device_user_id ON app_policies(device_user_id);
CREATE INDEX IF NOT EXISTS idx_app_policies_device_user_pkg ON app_policies(device_user_id, package_name);
