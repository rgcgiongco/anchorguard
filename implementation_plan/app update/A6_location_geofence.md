# Module A6: Location History & Geofence Alerts

## Goal
Show parents where their child has been and alert when they enter/leave designated safe zones.

## Context
- `LocationRepository.kt` exists with `getLocationHistory()` and `getLatestLocation()`
- `Location` entity has `device_id`, `latitude`, `longitude`, `timestamp_millis`
- `LocationForegroundService` declared in AndroidManifest
- Backend Device entity has no location fields yet — needs location storage
- Web console has no location/geofence UI

## Tasks

### A6.1: Backend - Location Storage
- [ ] Add `Location` table: id, device_id, latitude, longitude, accuracy, timestamp
- [ ] Add `POST /v1/devices/{id}/location` — Android app reports location
- [ ] Add `GET /v1/devices/{id}/location/history` — returns location history for time range
- [ ] Add `GET /v1/devices/{id}/location/latest` — returns most recent location

### A6.2: Android - Location Reporting
- [ ] `LocationForegroundService` already declared — verify it reports location periodically
- [ ] `LocationRepository` already exists — verify it inserts into local DB
- [ ] Sync local location history to backend periodically
- [ ] Respect `ACCESS_BACKGROUND_LOCATION` permission

### A6.3: Backend - Geofence
- [ ] Add `Geofence` table: id, parent_id, device_id, name, latitude, longitude, radius_meters, active
- [ ] Add `POST /v1/geofences` — create geofence
- [ ] Add `GET /v1/geofences` — list geofences for parent
- [ ] Add `DELETE /v1/geofences/{id}` — remove geofence
- [ ] Backend checks location reports against geofences, generates alert events

### A6.4: Web Console - Location Page
- [ ] Create `LocationPage.tsx` in web console
- [ ] Show map with child's latest location
- [ ] Show location history trail (last 24 hours)
- [ ] Show geofence list with add/edit/remove
- [ ] Show geofence alert events (entered/left safe zone)

### A6.5: Android - Geofence Alerts
- [ ] Android app checks geofence proximity locally (to save battery)
- [ ] When entering/leaving geofence, send alert event to backend
- [ ] Show notification on parent device (via FCM or polling)

## Deliverable
- Parents can see child's current location on map
- Location history for last 24+ hours
- Geofence zones with enter/leave alerts

## Test Credentials
| Account | Email | Password |
|---------|-------|----------|
| Admin | `anchor_admin@anchorguard.com` | `Gabby01172020!!!` |
| Parent | `parent@anchorguard.com` | `TestParent123!` |
