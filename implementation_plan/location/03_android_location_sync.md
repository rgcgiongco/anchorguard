# Module 3: Android Location Sync

## Goal
Upload unsynced locations to the backend via WorkManager background worker.

## Tasks
1. Create `LocationSyncWorker.kt` — `CoroutineWorker`:
   - Fetch unsynced locations from `LocationRepository`
   - Batch upload to `POST /api/v1/locations/batch` endpoint
   - On success: mark locations as synced in Room
   - On failure: retry with exponential backoff (WorkManager built-in retry)
   - Constraints: require network (`ConstraintSet` with `NETWORK_ANY`)
2. Create `LocationSyncScheduler.kt` — schedule periodic sync:
   - `PeriodicWorkRequest` every 15 minutes when network available
   - `OneTimeWorkRequest` on location capture (immediate upload attempt)
   - Cancel/reschedule when tracking is enabled/disabled
3. Wire sync trigger into `LocationRepository.saveLocation()`:
   - After inserting location, enqueue immediate sync work
4. Create `LocationBatchRequest.kt` — data class for batch upload:
   - `deviceId` (String)
   - `locations` (List of { latitude, longitude, accuracy, timestamp })
5. Update `SyncStatus` table to include `location` table tracking
6. Test batch upload to backend endpoint (verify 200/201 response)

## Deliverable
Locations automatically synced to backend via WorkManager with retry, network constraints, and batch upload.
