# Module 6: Backend Location Service

## Goal
Add geofence evaluation and location business logic on the backend.

## Tasks
1. Create `GeofenceZone.kt` — entity for safe zones:
   - `id` (UUID PK)
   - `deviceId` (UUID, FK to devices)
   - `name` (String, e.g. "Home", "School")
   - `latitude` (Double), `longitude` (Double)
   - `radiusMeters` (Integer)
   - `createdAt` (Instant)
2. Create `GeofenceZoneRepository.kt` — Spring Data JPA repository
3. Create `LocationService.kt` — business logic:
   - `storeLocations(deviceId, points)` — validate and persist batch
   - `getLatestLocation(deviceId)` — return most recent
   - `getLocationHistory(deviceId, from, to, limit)` — paginated history
   - `evaluateGeofence(lat, long, zone)` — distance check (Haversine formula)
   - `checkGeofenceAlerts(deviceId, latestLocation)` — return list of zone entry/exit events
4. Create `GeofenceAlert.kt` — audit event for zone breach:
   - `id`, `deviceId`, `zoneId`, `zoneName`, `alertType` (ENTER/EXIT), `occurredAt`
   - Store in `audit_log` table with action `GEOFENCE_ALERT`
5. Integrate geofence check into `POST /api/v1/locations/batch`:
   - After storing each point, evaluate against device's geofence zones
   - Emit audit log entry for entry/exit transitions
6. Create `GeofenceZoneController.kt` — CRUD for zones:
   - `GET /api/v1/devices/{id}/geofences` — list zones
   - `POST /api/v1/devices/{id}/geofences` — create zone
   - `DELETE /api/v1/geofences/{id}` — delete zone
7. Test geofence evaluation with known coordinates

## Deliverable
Backend stores locations, evaluates geofences, and emits audit alerts for zone entry/exit.
