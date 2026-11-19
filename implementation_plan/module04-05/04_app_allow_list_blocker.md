# Module 04: App Allow List / App Blocker

## Goal
Implement the app allow/block list functionality in the Android app so parents can approve or block installed apps on the child device.

## Context
- Device settings screen currently shows placeholder `AnchorInfoCard` for "App allow list"
- Backend: `DeviceController.kt` has `/v1/devices/{id}/policy` PATCH endpoint accepting `DevicePolicy` with `apps: AppPolicy[]`
- Backend DTO: `AppPolicy(packageName, appName, isAllowed, dailyLimitMinutes, blockSchedule, internetAccess)`
- Frontend: `DeviceAppsPage.tsx` exists in web console
- Android app: `AuthRepository.kt` has `pairDevice()` and device credentials; `NetworkConfig.kt` has `isDeviceProvisioned()`

## Tasks

### 04.1: Backend - App Policy CRUD (if not already complete)
- [ ] Verify `PATCH /v1/devices/{id}/policy` accepts `DevicePolicy` with `AppPolicy[]` array
- [ ] Verify backend stores per-app allow/block state, daily limits, internet access toggle
- [ ] Verify `GET /v1/devices/{id}/policy` returns current app policies
- [ ] Verify `GET /v1/devices/{id}/apps` returns installed apps with current policy state

### 04.2: Android - App List Screen
- [ ] Create `AppListScreen.kt` in `ui/screens/`
- [ ] Display list of installed apps on the device (package name, app name, icon)
- [ ] Show current allow/block state per app (toggle switch)
- [ ] Show daily limit minutes per app (input field)
- [ ] Show internet access toggle per app
- [ ] Pull-to-refresh to re-scan installed apps
- [ ] Search/filter apps by name

### 04.3: Android - App Policy API Integration
- [ ] Add `AppRepository.kt` in `auth/data/` or `device/data/`
- [ ] `fetchInstalledApps()`: GET `/v1/devices/{id}/apps` - returns installed apps
- [ ] `updateAppPolicy(packageName, isAllowed, dailyLimitMinutes, internetAccess)`: PATCH `/v1/devices/{id}/policy`
- [ ] `fetchAppPolicies()`: GET `/v1/devices/{id}/policy` - returns current policies
- [ ] Integrate with `AuthRepository` or create standalone `DevicePolicyRepository`

### 04.4: Android - DeviceSettingsScreen Integration
- [ ] Replace "App allow list" `AnchorInfoCard` placeholder with clickable button
- [ ] Navigate to `AppListScreen` on click
- [ ] Show badge/count of blocked apps on the settings screen

### 04.5: Backend - App Policy Enforcement (Device Admin / Accessibility)
- [ ] Verify `AnchorGuardAccessibilityService.kt` reads app policies and blocks apps
- [ ] Verify `AnchorGuardService.kt` foreground service enforces blocks
- [ ] Add app block list to enforcement logic (check against installed/running apps)

### 04.6: Web Console - App List Page (if needed)
- [ ] Verify `DeviceAppsPage.tsx` in web console connects to backend API
- [ ] Verify web console can update app policies remotely

## Deliverable
- Working "App allow list" in Device Settings → navigate to App List Screen
- Toggle allow/block per app with daily limit minutes
- Changes sync to backend and enforce on device

## Test Credentials
| Account | Email | Password |
|---------|-------|----------|
| Admin | `anchor_admin@anchorguard.com` | `Gabby01172020!!!` |
| Parent | `parent@anchorguard.com` | `TestParent123!` |
