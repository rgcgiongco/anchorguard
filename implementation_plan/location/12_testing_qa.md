# Module 12: Testing & QA — Location Tracking

## Goal
Verify end-to-end location tracking across all three layers.

## Tasks
1. Android client tests:
   - Location permission request flow (grant/deny rationale)
   - FusedLocationProviderClient returns valid coordinates (emulator mock location)
   - Foreground service starts/stops with tracking toggle
   - Room database stores locations with `synced=false` initially
   - WorkManager uploads batch to backend (verify 202 response)
   - Locations marked `synced=true` after successful upload
2. Backend API tests:
   - `POST /api/v1/locations/batch` accepts batch (202 Accepted)
   - `GET /api/v1/devices/{id}/locations` returns paginated history
   - `GET /api/v1/devices/{id}/locations/current` returns latest point
   - Geofence zone CRUD works (create, list, delete)
   - Geofence alert emitted on zone entry/exit (check audit_log)
   - CORS works from web console origin
3. Web console tests:
   - `/locations` route loads (HTTP 200)
   - Map renders with device markers
   - Location history list displays points
   - Geofence zone editor creates/deletes zones
   - Tracking toggle updates device setting
   - Empty state shows when no location data
   - Mobile responsive (stacked layout on narrow screens)
4. Cross-layer E2E:
   - Android sends location → backend stores → web console displays on map
   - Parent creates geofence in web console → Android device enters zone → audit alert logged
5. Performance:
   - Map loads within 2 seconds
   - Batch upload of 100 locations completes under 5 seconds
   - Location history pagination works with large datasets

## Deliverable
Location tracking verified end-to-end: Android captures → backend stores → web console displays, with geofence alerts working.
