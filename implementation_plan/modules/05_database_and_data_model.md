# Module 05 — Database and Data Model

## Overview
AnchorGuard uses two databases:
- **Local (Android)**: Room SQLite — offline-first device state, policies cached from cloud, usage events before sync
- **Remote (backend)**: PostgreSQL — system of record, parent accounts, devices, policies, audit events

Schema names are original AnchorGuard names (not reference names).

## PostgreSQL Schema (backend)

### Tables
- `parents` — parent accounts (id, email, password_hash, display_name, created_at, updated_at)
- `devices` — registered child devices (id, parent_id, device_name, device_id/fcm token, os_version, api_level, last_seen_at, is_active, pairing_token, pairing_expires_at)
- `device_users` — per-device child profiles (id, device_id, child_name, age_birthday, avatar_color, is_primary)
- `app_policies` — per-app rules (id, device_user_id, package_name, app_name, category, blocked, daily_limit_minutes, schedule_start, schedule_end, weekdays_mask, blocked_message)
- `device_settings` — device-level settings (id, device_id, key, value, updated_at)
- `usage_events` — app usage events (id, device_user_id, package_name, event_type [FOREGROUND/BACKGROUND/LAUNCH/CLOSE], started_at, ended_at, duration_ms)
- `refresh_tokens` — refresh token store (id, parent_id, token_hash, expires_at, revoked_at)
- `password_reset_tokens` — password reset (id, parent_id, token_hash, expires_at, used_at)
- `audit_log` — enforcement/audit events (id, device_id, actor [PARENT/SYSTEM], action, target_package, occurred_at, details_json)

### Migrations
- `V1__init.sql` — core tables and indexes (exists in backend)
- `V2__device_users_and_app_policies.sql` — device_users + app_policies + FKs + indexes
- `V3__usage_events_and_audit_log.sql` — usage_events + audit_log + retention indexes
- `V4__refresh_and_reset_tokens.sql` — refresh_tokens + password_reset_tokens
- Flyway managed; each migration is additive where possible; no destructive changes in MVP

## Room Schema (Android)

### Entities
- `DeviceUser` — id (PK), deviceId, childName, ageBirthday, avatarColor, isPrimary
- `AppPolicy` — id (PK), deviceUserId, packageName, appName, category, blocked, dailyLimitMinutes, scheduleStart, scheduleEnd, weekdaysMask, blockedMessage
- `DeviceSetting` — id (PK), deviceId, key, value, updatedAt
- `UsageEvent` — id (PK), deviceUserId, packageName, eventType, startedAt, endedAt, durationMs, synced (boolean)
- `SyncStatus` — id (PK), tableName, lastSyncedAt, pendingCount

### DAOs
- `DeviceUserDao` — insert, update, delete, getAll, getById, getByDeviceId
- `AppPolicyDao` — insert, update, delete, getAll, getByDeviceUserId, getByPackageName, getBlocked
- `DeviceSettingDao` — get, set, delete, getAll
- `UsageEventDao` — insert, updateSynced, getUnsynced, getAll
- `SyncStatusDao` — get, update

### Database
- `AnchorGuardDatabase` — Room database, version 1, entities list, DAOs abstract methods
- Migration strategy: Room migrations (1->2->3) with `Migration` objects; fallbackToDestructiveMigration only in debug builds

## Sync Strategy
- Local is offline-first; cloud is system of record
- Usage events written locally first, flagged `synced=false`
- Background WorkManager uploads unsynced events to `/api/v1/usage/batch` when network available
- Policy changes downloaded from `/api/v1/policies` and merged into local Room on app launch / foreground
- Last-synced timestamps per table to avoid full re-downloads
- Conflict resolution: cloud wins (last-write-wins); local events queued and retried with exponential backoff

## Clean Room Rules
- Room entity/DAO/database names are original AnchorGuard names
- No copied entity field names from reference analysis
- Table/column names in PostgreSQL are snake_case original names
- Do not reference obfuscated reference schema/table names
