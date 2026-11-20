-- V10: Shared device mode (BOTH role) support
ALTER TABLE devices ADD COLUMN role VARCHAR(20) NOT NULL DEFAULT 'PARENT';
ALTER TABLE devices ADD COLUMN active_mode VARCHAR(20) NOT NULL DEFAULT 'parent';
ALTER TABLE devices ADD COLUMN last_mode_switch_at TIMESTAMPTZ;
ALTER TABLE devices ADD COLUMN switched_by_user_id UUID;
ALTER TABLE devices ADD CONSTRAINT fk_devices_switched_by FOREIGN KEY (switched_by_user_id) REFERENCES parents(id);
CREATE INDEX IF NOT EXISTS idx_devices_role ON devices(role);
CREATE INDEX IF NOT EXISTS idx_devices_active_mode ON devices(active_mode);

ALTER TABLE parents ADD COLUMN parent_pin_hash VARCHAR(255);
