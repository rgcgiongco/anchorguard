# Module 8: Web Console Location API

## Goal
Add API endpoint functions for location data in the web console.

## Tasks
1. Add to `src/api/endpoints.ts` — `locationsApi` object:
   - `batchUpload(payload: { deviceId: string; locations: LocationPoint[] })` — POST `/api/v1/locations/batch` (device auth, used by Android sync, exposed for completeness)
   - `getLocationHistory(deviceId: string, params?: LocationHistoryParams)` — GET `/api/v1/devices/{id}/locations`
   - `getCurrentLocation(deviceId: string)` — GET `/api/v1/devices/{id}/locations/current`
   - `getGeofenceZones(deviceId: string)` — GET `/api/v1/devices/{id}/geofences`
   - `createGeofenceZone(deviceId: string, payload)` — POST `/api/v1/devices/{id}/geofences`
   - `deleteGeofenceZone(zoneId: string)` — DELETE `/api/v1/geofences/{id}`
2. Add `LOCATION_API_BASE` constant using `API_BASE_URL` from `src/lib/constants.ts`
3. Add error handling via existing `getErrorMessage()` utility
4. Verify `tsc --noEmit` passes

## Deliverable
API endpoint functions for all location-related REST endpoints.
