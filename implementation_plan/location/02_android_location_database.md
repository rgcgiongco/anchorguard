# Module 2: Android Location Database

## Goal
Store captured locations locally in Room for offline-first sync.

## Tasks
1. Create `LocationEntity.kt` — Room entity:
   - `id` (PK, auto-generate)
   - `deviceId` (String, indexed)
   - `latitude` (Double)
   - `longitude` (Double)
   - `accuracy` (Float)
   - `timestamp` (Long, epoch millis)
   - `synced` (Boolean, default false)
   - `createdAt` (Long, epoch millis)
2. Create `LocationDao.kt` — DAO interface:
   - `insert(location: LocationEntity)` — insert single location
   - `insertAll(locations: List<LocationEntity>)` — batch insert
   - `getUnsyncedLocations()` — List<LocationEntity> where synced == false
   - `markAsSynced(ids: List<Long>)` — update synced flag
   - `getLocationsSince(deviceId, since)` — query history for map display
   - `getLatestLocation(deviceId)` — most recent location
3. Add `LocationEntity` to `AnchorGuardDatabase` entities list
4. Create Room migration `1->2` (add location table) with `Migration` object
5. Create `LocationRepository.kt` — repository layer:
   - `saveLocation(lat, long, accuracy, timestamp)` — write to Room
   - `getUnsynced()` — return unsynced locations for upload
   - `markSynced(ids)` — mark uploaded locations as synced
   - `getLocationHistory(deviceId, limit)` — return recent history for UI
6. Verify database migration applies cleanly on existing installs

## Deliverable
Locations stored locally in Room with offline-first sync support, migration ready for existing installations.
