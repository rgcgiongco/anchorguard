-- V5: role-based access control (RBAC) and system administrator seed.
-- Distinguishes the super-admin (system administrator) from regular parent users.

CREATE TABLE IF NOT EXISTS roles (
  role_id SMALLINT PRIMARY KEY,
  name TEXT NOT NULL UNIQUE,
  description TEXT,
  is_system BOOLEAN NOT NULL DEFAULT FALSE
);

INSERT INTO roles (role_id, name, description, is_system) VALUES
  (1, 'parent', 'Regular parent user with access to their own linked devices', TRUE),
  (2, 'admin', 'System administrator with full platform-wide access', TRUE)
ON CONFLICT (role_id) DO NOTHING;

ALTER TABLE parents ADD COLUMN IF NOT EXISTS role_id SMALLINT NOT NULL DEFAULT 1 REFERENCES roles(role_id);
CREATE INDEX IF NOT EXISTS idx_parents_role_id ON parents(role_id);

-- Seed the system administrator account.
-- Password: Gabby01172020!!!  (Argon2id hash produced by Spring Security Argon2PasswordEncoder)
INSERT INTO parents (email, password_hash, name, email_verified, role_id)
VALUES (
  'anchor_admin',
  '$argon2id$v=19$m=16384,t=2,p=1$30ksAOOBF2O7SaBanzDmdw$NAIaIKNMfllgs/v9aZqFhwiPcnQISwKIl1REviIkajw',
  'System Administrator',
  TRUE,
  2
)
ON CONFLICT (email) DO UPDATE SET password_hash = EXCLUDED.password_hash, role_id = 2, email_verified = TRUE;