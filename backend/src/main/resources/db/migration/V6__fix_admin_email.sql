-- V6: Update system administrator email to valid format
UPDATE parents SET email = 'anchor_admin@anchorguard.com' WHERE email = 'anchor_admin' AND role_id = 2;
