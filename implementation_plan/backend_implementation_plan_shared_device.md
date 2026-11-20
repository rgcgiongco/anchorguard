# Backend Implementation Plan: Shared "Both" Device Mode

## Goal
Extend the Spring Boot backend to support devices with role `BOTH`, track active mode (`parent` / `child`), sync mode state between Android client and server, and expose mode information to the Parent Dashboard (web console).

## Scope
- **In scope:** Backend API (`backend/` module), database schema, web console visibility
- **Out of scope:** Android client UI (addressed in separate app plan)

## Files to Modify / Create

### [MODIFY] `backend/src/main/kotlin/com/anchorguard/api/device/Entities.kt`
- Add field to `Device` entity:
  ```kotlin
  @Column(nullable = false)
  var role: DeviceRole = DeviceRole.PARENT // enum: PARENT, CHILD, BOTH
  ```
- Add field to `Device` entity (or new `DeviceSession` entity):
  ```kotlin
  @Column(nullable = false)
  var activeMode: String = "parent" // "parent" or "child"
  var lastModeSwitchAt: LocalDateTime? = null
  var switchedByUserId: Long? = null
  ```
- Add enum `DeviceRole { PARENT, CHILD, BOTH }`

### [MODIFY] `backend/src/main/kotlin/com/anchorguard/api/device/Dto.kt`
- Add `role` and `activeMode` to `DeviceDto` / `DeviceResponse`
- Add request DTO `SwitchModeRequest { mode: "parent" | "child", pin: String }`
- Add response DTO `SwitchModeResponse { success: Boolean, currentMode: String }`

### [MODIFY] `backend/src/main/kotlin/com/anchorguard/api/device/Repositories.kt`
- Add query methods:
  - `findByRole(role: DeviceRole): List<Device>`
  - `findByUserIdAndRole(userId: Long, role: DeviceRole): List<Device>`

### [MODIFY] `backend/src/main/kotlin/com/anchorguard/api/device/DeviceService.kt`
- Add business logic:
  - `switchDeviceMode(deviceId: Long, requestedMode: String, pin: String): SwitchModeResponse`
    - Validate device exists and role == `BOTH`
    - Validate PIN against stored parent PIN (or via auth service)
    - Update `activeMode`, `lastModeSwitchAt`, `switchedByUserId`
    - Emit audit event
  - `getDeviceMode(deviceId: Long): String` — return current active mode
  - `isEnforcementActive(deviceId: Long): Boolean` — true if role == `CHILD` OR (role == `BOTH` AND activeMode == `child`)

### [MODIFY] `backend/src/main/kotlin/com/anchorguard/api/device/DeviceController.kt`
- Add endpoints:
  - `GET /api/devices/{id}/mode` — returns current active mode
  - `POST /api/devices/{id}/mode/switch` — switch mode (requires auth + PIN validation)
  - `GET /api/devices/shared` — list all `BOTH` devices for parent dashboard

### [MODIFY] `backend/src/main/kotlin/com/anchorguard/api/sync/SyncService.kt`
- Add sync logic for mode state:
  - On client sync: compare client-reported `activeMode` with server, resolve conflicts (server wins or last-write-wins)
  - Push mode changes to connected clients via FCM or polling (optional MVP: client polls on app launch)

### [MODIFY] `backend/src/main/kotlin/com/anchorguard/api/auth/AuthService.kt`
- Add PIN management:
  - `setParentPin(userId: Long, pin: String)` — hash and store
  - `validateParentPin(userId: Long, pin: String): Boolean`

### [MODIFY] `backend/src/main/kotlin/com/anchorguard/api/security/JwtService.kt`
- Add `deviceRole` and `activeMode` claims to JWT token so client can decode without extra API call
- Or add custom `Authentication` principal with mode info

### [MODIFY] `backend/src/main/kotlin/com/anchorguard/api/audit/AuditLog.kt`
- Add audit entry type `MODE_SWITCHED`
- Log: deviceId, fromMode, toMode, switchedBy, timestamp, ipAddress

### [NEW] `backend/src/main/resources/db/migration/Vxx__add_shared_device_mode.sql`
- Flyway migration:
  ```sql
  ALTER TABLE devices ADD COLUMN role VARCHAR(20) NOT NULL DEFAULT 'PARENT';
  ALTER TABLE devices ADD COLUMN active_mode VARCHAR(20) NOT NULL DEFAULT 'parent';
  ALTER TABLE devices ADD COLUMN last_mode_switch_at TIMESTAMP;
  ALTER TABLE devices ADD COLUMN switched_by_user_id BIGINT;
  ALTER TABLE devices ADD CONSTRAINT fk_devices_switched_by FOREIGN KEY (switched_by_user_id) REFERENCES users(id);
  CREATE INDEX idx_devices_role ON devices(role);
  CREATE INDEX idx_devices_active_mode ON devices(active_mode);
  ```

### [MODIFY] `backend/src/main/kotlin/com/anchorguard/api/admin/AdminController.kt` (or AdminDashboardPage)
- Add endpoint `GET /api/admin/devices/shared-mode` — list devices in `BOTH` role with current active mode
- Add audit log viewer for mode switches

### [MODIFY] Web Console (`web-console/src/pages/AdminDashboardPage.tsx` or DeviceDetailPage)
- Add "Device Mode" badge (Parent / Child / Shared)
- For shared devices: show current active mode and "Switch Mode" button (calls backend API)
- Add mode switch history table from audit logs

## API Endpoints Summary

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| GET | `/api/devices/{id}/mode` | Parent | Get current active mode |
| POST | `/api/devices/{id}/mode/switch` | Parent + PIN | Switch between parent/child mode |
| GET | `/api/devices/shared` | Parent | List all shared (BOTH) devices |
| GET | `/api/admin/devices/shared-mode` | Admin | Admin view of all shared devices |

## Testing
- Unit tests: `DeviceService.switchDeviceMode` with valid/invalid PIN
- Integration tests: full flow — set role to BOTH, switch mode, verify enforcement flag changes
- Contract test: Android client sends `mode` in sync payload, backend persists and returns it

## Risks & Mitigations
| Risk | Mitigation |
|------|-----------|
| Client and server mode out of sync | SyncService reconciles on app launch; server is source of truth |
| PIN brute force | Rate-limit `/mode/switch` endpoint (e.g., 5 attempts/minute) |
| Non-BOTH device tries to switch mode | Controller validates `role == BOTH`, returns 403 otherwise |
| Lost parent PIN | Password recovery flow (existing auth system) resets PIN |

## Verification
- `./gradlew :backend:bootTest` or `./gradlew :backend:test` — run backend tests
- `curl` the new endpoints manually
- Web console: open Admin Dashboard, verify shared device mode badge and switch button work