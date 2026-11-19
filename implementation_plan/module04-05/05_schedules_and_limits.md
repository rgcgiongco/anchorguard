# Module 05: Schedules + Per-App Limits

## Goal
Implement daily/weekly schedules, bedtime windows, and per-app time limits in the Android app so parents can control when and how long their child can use the device and specific apps.

## Context
- Device settings screen currently shows placeholder `AnchorInfoCard` for "Schedules" and "Per-app limits"
- Backend DTO: `DeviceSettings(advancedTimer, backgroundMode, tamperProof, bootStart, keepAlive, bruteForcePin, blockIntervalMs, appTitle, weeklySchedule)`
- Backend DTO: `AppPolicy(dailyLimitMinutes?, blockSchedule?)`
- Web console: `DeviceSchedulePage.tsx` exists
- Android: `DeviceSettingsScreen.kt` has placeholder for Location tracking only

## Tasks

### 05.1: Backend - Schedule & Limit Endpoints
- [ ] Verify `GET /v1/devices/{id}/schedule` returns weekly schedule
- [ ] Verify `PUT /v1/devices/{id}/schedule` updates weekly schedule
- [ ] Verify `PATCH /v1/devices/{id}/policy` supports `blockSchedule` per app
- [ ] Verify `GET /v1/devices/{id}/policy` returns `blockSchedule` per app
- [ ] Verify backend enforces schedule-based blocking (device lock during scheduled windows)

### 05.2: Android - Schedule Screen
- [ ] Create `ScheduleScreen.kt` in `ui/screens/`
- [ ] Display weekly schedule grid (Monday–Sunday, time slots)
- [ ] Allow setting "allowed hours" per day (e.g., 8am–8pm)
- [ ] Allow setting "bedtime window" per day (e.g., 9pm–7am)
- [ ] Toggle schedule enforcement on/off
- [ ] Save to backend via API

### 05.3: Android - Per-App Limits Screen
- [ ] Create `PerAppLimitsScreen.kt` in `ui/screens/`
- [ ] List installed apps with current time limits
- [ ] Set daily limit minutes per app (0 = unlimited)
- [ ] Set internet access toggle per app (allow/block internet)
- [ ] Show remaining time per app today (from backend usage data)
- [ ] Save changes to backend

### 05.4: Android - DeviceSettingsScreen Integration
- [ ] Replace "Schedules" `AnchorInfoCard` placeholder with clickable button → `ScheduleScreen`
- [ ] Replace "Per-app limits" `AnchorInfoCard` placeholder with clickable button → `PerAppLimitsScreen`
- [ ] Show summary badges (e.g., "3 apps limited", "Schedule active: 8am-8pm")

### 05.5: Backend - Schedule Enforcement
- [ ] Verify `AnchorGuardService.kt` checks current time against schedule
- [ ] Verify device locks outside allowed hours (bedtime enforcement)
- [ ] Verify per-app time limits are enforced (block app when daily limit reached)
- [ ] Verify `blockIntervalMs` from `DeviceSettings` controls block check frequency

### 05.6: Android - DeviceSettings Data Model
- [ ] Add `DeviceSettings` data class in `auth/data/` or `device/data/`
- [ ] `fetchDeviceSettings()`: GET `/v1/devices/{id}` (detail response includes settings)
- [ ] `updateDeviceSettings(settings)`: PATCH `/v1/devices/{id}` with `DevicePatchRequest`
- [ ] Integrate with `AuthRepository` or standalone `DeviceSettingsRepository`

### 05.7: Web Console - Schedule & Limits Pages (if needed)
- [ ] Verify `DeviceSchedulePage.tsx` connects to backend schedule API
- [ ] Verify web console can update schedules and per-app limits remotely

## Deliverable
- Working "Schedules" screen with weekly time windows
- Working "Per-app limits" screen with daily minutes per app
- Changes sync to backend and enforce on device (schedule lock + app time limits)

## Test Credentials
| Account | Email | Password |
|---------|-------|----------|
| Admin | `anchor_admin@anchorguard.com` | `Gabby01172020!!!` |
| Parent | `parent@anchorguard.com` | `TestParent123!` |
