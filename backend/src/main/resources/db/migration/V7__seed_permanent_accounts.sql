-- V7: Seed permanent system accounts (idempotent)
-- These accounts survive database resets and are never deleted.

-- System Administrator (already seeded in V5/V6, ensured here)
INSERT INTO parents (email, password_hash, name, email_verified, role_id)
VALUES (
  'anchor_admin@anchorguard.com',
  '$argon2id$v=19$m=16384,t=2,p=1$30ksAOOBF2O7SaBanzDmdw$NAIaIKNMfllgs/v9aZqFhwiPcnQISwKIl1REviIkajw',
  'System Administrator',
  TRUE,
  2
)
ON CONFLICT (email) DO UPDATE SET
  role_id = 2,
  email_verified = TRUE;

-- Permanent Test Parent Account
INSERT INTO parents (email, password_hash, name, email_verified, role_id)
VALUES (
  'parent@anchorguard.com',
  '$argon2id$v=19$m=16384,t=2,p=1$placeholder_hash_for_migration$placeholder',
  'Test Parent',
  TRUE,
  1
)
ON CONFLICT (email) DO NOTHING;
