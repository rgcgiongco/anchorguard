# Module A5: Usage Reports & Analytics

## Goal
Provide parents with visibility into what apps their child uses, how long, and when — plus location history.

## Context
- `ReportsScreen.tsx` exists in web console — placeholder
- `UsageAggregate` entity exists in backend (`api/types.ts`)
- `UsageEvent` entity may exist in backend for raw events
- `LocationRepository.kt` exists in Android app with `getLocationHistory()`
- `DeviceDetailPage.tsx` exists in web console

## Tasks

### A5.1: Backend - Usage Data Endpoints
- [ ] Verify `GET /v1/reports/usage` returns usage aggregates per device
- [ ] Add `GET /v1/devices/{id}/usage` — per-device usage breakdown by app
- [ ] Add `GET /v1/devices/{id}/usage/export` — CSV export of usage data
- [ ] Verify backend stores app usage events (package name, timestamp, duration)
- [ ] Android app reports usage events to backend periodically

### A5.2: Android - Usage Reporting
- [ ] Create `UsageReporter.kt` in `enforcement/` or `data/`
- [ ] Periodically (every N minutes) send app usage events to backend
- [ ] Report: package name, app name, foreground time, timestamp
- [ ] Respect battery optimization (don't report too frequently)

### A5.3: Web Console - Reports Page
- [ ] Update `ReportsScreen.tsx` — replace placeholder
- [ ] Show usage charts per child device (bar chart: time per app)
- [ ] Show "Top apps used today" list
- [ ] Show "Screen time trend" (last 7 days)
- [ ] Export to CSV button

### A5.4: Web Console - Device Detail
- [ ] Verify `DeviceDetailPage.tsx` shows per-app usage for selected device
- [ ] Show app usage timeline (when each app was opened/closed)

## Deliverable
- Parents can see which apps their child used and for how long
- Usage data syncs from child device to backend
- Web console displays usage charts and export

## Files to Create/Modify
- Create: `app/src/main/java/com/anchorguard/client/enforcement/UsageReporter.kt`
- Create: `app/src/main/java/com/anchorguard/client/data/usage/` (entity, DAO, repository)
- Modify: Backend `reports/` controller — add usage endpoints
- Modify: Web console `ReportsScreen.tsx` — usage charts
- Modify: Web console `DeviceDetailPage.tsx` — per-app usage

## Test Scenario
1. Child uses tablet for 30 minutes (YouTube 15 min, Games 10 min, Chrome 5 min)
2. Usage events sync to backend
3. Parent opens web console → Reports → sees breakdown: YouTube 15m, Games 10m, Chrome 5m
4. Parent clicks "Export CSV" → downloads usage data
