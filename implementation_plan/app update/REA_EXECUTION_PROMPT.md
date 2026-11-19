# REA Execution Prompt: AnchorGuard App Update — Missing Enforcement & Management Features

## Context
Project: AnchorGuard — Android phone/tablet client + web parent console + self-hosted Spring Boot backend
Website: `https://anchorguard.tail78464.ts.net/` (live)
Backend API: `https://anchorguard.tail78464.ts.net/api/v1/` (live)
Android app: Native Kotlin + Jetpack Compose at `/home/anchor/AnchorGuard/app/`
Backend: Spring Boot 3.4.3 + Kotlin + PostgreSQL 17 at `/home/anchor/AnchorGuard/backend/`

## Problem
The Android app currently has working device registration + pairing + parent dashboard, but is missing critical enforcement and management features:

1. **No enforcement permission guidance** — Device Admin, Accessibility, Overlay, Battery Optimization are requested but user is not guided through granting them, and code doesn't verify they're active
2. **No remote device lock** — Parent cannot lock child device remotely
3. **No tamper protection** — Child can uninstall/disable AnchorGuard easily
4. **No boot persistence** — Enforcement doesn't survive reboot
5. **No multi-child profiles** — Users screen is placeholder
6. **No usage reports** — Reports screen is placeholder
7. **No location history / geofence** — Location tracking exists but no history or geofence UI
8. **No SOS / emergency alert** — Child cannot signal parent in emergency

## EXCLUDED (already covered in module04-05)
- App Blocker / Allow List
- Scheduler / Bedtime Windows
- Per-App Time Limits

## Modules to Execute
Module files at `/home/anchor/AnchorGuard/implementation_plan/app update/`:
- `A1_enforcement_permissions.md`
- `A2_remote_device_lock.md`
- `A3_tamper_boot_protection.md`
- `A4_multi_child_profiles.md`
- `A5_usage_reports.md`
- `A6_location_geofence.md`
- `A7_sos_emergency.md`

## Execution Order (recommended)
1. **A1** — Enforcement permissions (foundation for everything else)
2. **A3** — Tamper + boot persistence (protect the enforcement)
3. **A2** — Remote device lock (requires Device Admin from A1)
4. **A4** — Multi-child profiles (management)
5. **A5** — Usage reports (visibility)
6. **A6** — Location + geofence (safety)
7. **A7** — SOS / emergency (safety)

## Instructions
1. Read each module file in order
2. Reverse engineer existing codebase structure:
   - Backend: `DeviceController.kt`, `DeviceService.kt`, `Dto.kt` in `com.anchorguard.api.device`
   - Backend: `DeviceRepository.kt`, `Device.kt` entity
   - Android: `AuthRepository.kt`, `SecureSessionStore.kt`, `NetworkConfig.kt`
   - Android: `DeviceSettingsScreen.kt` (current placeholder)
   - Android: `AnchorGuardService.kt`, `AnchorGuardAccessibilityService.kt`, `AnchorGuardDeviceAdminReceiver.kt`, `BootCompletedReceiver.kt`
   - Android: `LocationRepository.kt`, `LocationForegroundService.kt`
   - Web console: `ReportsScreen.tsx`, `DeviceDetailPage.tsx`, `UsersPage.tsx`
3. Implement modules sequentially
4. Verify each module compiles and works before proceeding
5. Connect to live backend API at `https://anchorguard.tail78464.ts.net/api/v1/`

## Deliverable
Working Android app features:
- Enforcement permission guide + verification
- Remote device lock/unlock
- Tamper protection + boot persistence
- Multi-child device management
- Usage reports & analytics
- Location history + geofence alerts
- SOS / emergency alert

## Test Credentials
| Account | Email | Password |
|---------|-------|----------|
| Admin | `anchor_admin@anchorguard.com` | `Gabby01172020!!!` |
| Parent | `parent@anchorguard.com` | `TestParent123!` |

## Notes
- Do NOT commit the signing keystore (`anchorguard-release.jks`) — it's not in the repo
- Test on a physical Android device (emulator cannot properly test Device Admin, Accessibility, overlay, battery optimization)
- Web console at `https://anchorguard.tail78464.ts.net/` is live — verify backend endpoints connect
