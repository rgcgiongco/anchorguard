# Module 0: Project Foundation — Location Tracking

## Goal
Set up dependencies, permissions, and base configuration for GPS location tracking across all three layers.

## Tasks
1. Add location dependencies to Android client `gradle/libs.versions.toml`:
   - `com.google.android.gms:play-services-location` (FusedLocationProviderClient)
   - `androidx.work:work-runtime-ktx` (background sync)
   - `com.google.maps:google-maps-services` (optional, geocoding)
2. Add location permissions to `AndroidManifest.xml`:
   - `android.permission.ACCESS_FINE_LOCATION`
   - `android.permission.ACCESS_COARSE_LOCATION`
   - `android.permission.ACCESS_BACKGROUND_LOCATION` (optional, for continuous tracking)
   - `android.permission.FOREGROUND_SERVICE`
   - `android.permission.FOREGROUND_SERVICE_LOCATION`
   - `android.permission.POST_NOTIFICATIONS`
3. Add location permission strings to `res/values/strings.xml` with rationale text
4. Add Location feature flag to backend `application.properties`:
   - `anchorguard.location.enabled=true`
   - `anchorguard.location.geofence.enabled=true`
5. Create `src/lib/constants.ts` additions in web console for location feature (map provider config)
6. Verify project builds after dependency additions

## Deliverable
Dependencies installed, permissions declared, project builds cleanly with location support enabled.
