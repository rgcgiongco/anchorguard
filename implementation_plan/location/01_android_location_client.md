# Module 1: Android Location Client

## Goal
Implement GPS location capture on the Android client using FusedLocationProviderClient.

## Tasks
1. Create `LocationManager.kt` — singleton wrapper around `FusedLocationProviderClient`:
   - `requestLocationUpdates()` with `LocationRequest` (PRIORITY_BALANCED_POWER_ACCURACY, 5 min interval, 1 min fastest)
   - `getLastLocation()` — retrieve cached last known location
   - `removeLocationUpdates()` — stop tracking when disabled
   - Permission check helper (`hasFineLocationPermission()`)
2. Create `LocationService.kt` — foreground service for continuous background tracking:
   - `LocationForegroundService : Service()` with foreground notification
   - Start/stop tracking based on parent toggle from web console
   - Bind to `LocationManager` for periodic location capture
3. Create `LocationPermissionHelper.kt` — runtime permission flow:
   - Request `ACCESS_FINE_LOCATION` and `ACCESS_COARSE_LOCATION`
   - Handle permission result callbacks
   - Check if background location is granted
   - Show rationale dialog if permission denied permanently
4. Create `LocationTrackingController.kt` — coordinate between UI and service:
   - Enable/disable tracking via device settings (paired with parent toggle)
   - Read current tracking state from `DeviceSetting` (key: `location_tracking_enabled`)
   - Start/stop foreground service accordingly
5. Wire tracking toggle into existing Device Settings screen (Jetpack Compose)
6. Test location capture returns valid coordinates in emulator/device

## Deliverable
Android client can capture GPS location via FusedLocationProviderClient with proper permissions and foreground service for background tracking.
