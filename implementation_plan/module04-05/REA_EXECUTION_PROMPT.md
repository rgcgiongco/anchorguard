# REA Execution Prompt: AnchorGuard App Allow List + Schedules + Limits (Modules 04-05)

## Context
Project: AnchorGuard — Android phone/tablet client + web parent console + self-hosted Spring Boot backend
Website: `https://anchorguard.tail78464.ts.net/` (live)
Backend API: `https://anchorguard.tail78464.ts.net/api/v1/` (live)
Android app: Native Kotlin + Jetpack Compose at `/home/anchor/AnchorGuard/app/`
Backend: Spring Boot 3.4.3 + Kotlin + PostgreSQL 17 at `/home/anchor/AnchorGuard/backend/`

## Problem
The Device Settings screen in the Android app shows 3 placeholder `AnchorInfoCard` items:
1. "App allow list" — "Approve or block installed apps (Module 04/05 integration)"
2. "Schedules" — "Daily/weekly schedules and bedtime windows placeholder"
3. "Per-app limits" — "Time limits and internet toggles per app placeholder"

These are non-clickable info cards. The actual app blocking, scheduling, and per-app limits features need to be implemented end-to-end.

## Modules to Execute
Module files at `/home/anchor/AnchorGuard/implementation_plan/module04-05/`:
- `04_app_allow_list_blocker.md`
- `05_schedules_and_limits.md`

## Execution Order
1. **Module 04** — App Allow List / Blocker (AppListScreen, API integration, DeviceSettings integration)
2. **Module 05** — Schedules + Per-App Limits (ScheduleScreen, PerAppLimitsScreen, enforcement)

## Instructions
1. Read each module file in order
2. Reverse engineer existing codebase structure:
   - Backend: `DeviceController.kt`, `DeviceService.kt`, `Dto.kt` in `com.anchorguard.api.device`
   - Backend: `DeviceRepository.kt`, `Device.kt` entity
   - Android: `AuthRepository.kt`, `SecureSessionStore.kt`, `NetworkConfig.kt`, `NetworkModule.kt`
   - Android: `DeviceSettingsScreen.kt` (current placeholder)
   - Android: `AnchorGuardService.kt`, `AnchorGuardAccessibilityService.kt` (enforcement)
   - Web console: `DeviceAppsPage.tsx`, `DeviceSchedulePage.tsx`
3. Implement Module 04 fully before Module 05
4. Verify each module compiles and works before proceeding
5. Use existing backend endpoints (`/v1/devices/{id}/policy`, `/v1/devices/{id}/schedule`, etc.)
6. Connect to live backend API at `https://anchorguard.tail78464.ts.net/api/v1/`

## Deliverable
Working Android app features:
- **App allow list**: Toggle allow/block per app, set daily limit minutes, internet access per app
- **Schedules**: Weekly allowed hours, bedtime windows, schedule enforcement
- **Per-app limits**: Daily time limits per app, internet access toggle
- All changes sync to backend and enforce on child device

## Test Credentials
| Account | Email | Password |
|---------|-------|----------|
| Admin | `anchor_admin@anchorguard.com` | `Gabby01172020!!!` |
| Parent | `parent@anchorguard.com` | `TestParent123!` |

## Notes
- Do NOT commit the signing keystore (`anchorguard-release.jks`) — it's not in the repo
- Test on a physical Android device (emulator cannot properly test Device Admin, Accessibility, overlay)
- Web console at `https://anchorguard.tail78464.ts.net/` already has DeviceAppsPage and DeviceSchedulePage — verify they connect to the same backend endpoints
