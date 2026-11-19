# Module 4: Backend Location Entity

## Goal
Add Location table to PostgreSQL with Flyway migration.

## Tasks
1. Create Flyway migration `V5__location_tracking.sql`:
   ```sql
   CREATE TABLE locations (
     id BIGSERIAL PRIMARY KEY,
     device_id UUID NOT NULL REFERENCES devices(id) ON DELETE CASCADE,
     latitude DOUBLE PRECISION NOT NULL,
     longitude DOUBLE PRECISION NOT NULL,
     accuracy DOUBLE PRECISION,
     timestamp TIMESTAMPTZ NOT NULL,
     created_at TIMESTAMPTZ DEFAULT NOW()
   );
   CREATE INDEX idx_locations_device_id ON locations(device_id);
   CREATE INDEX idx_locations_timestamp ON locations(timestamp DESC);
   ```
2. Create `Location.kt` — JPA entity:
   - `id` (Long, @Id, @GeneratedValue)
   - `deviceId` (UUID, @Column, @ManyToOne to Device)
   - `latitude` (Double)
   - `longitude` (Double)
   - `accuracy` (Double, nullable)
   - `timestamp` (Instant)
   - `createdAt` (Instant)
3. Create `LocationRepository.kt` — Spring Data JPA repository:
   - `save(location: Location)`
   - `findByDeviceIdOrderByTimestampDesc(deviceId, pageable)`
   - `findFirstByDeviceIdOrderByTimestampDesc(deviceId)` — latest
   - `findByDeviceIdAndTimestampAfter(deviceId, since, pageable)` — history range
4. Add Location entity to Spring Boot entity scan package
5. Verify migration applies cleanly against existing database

## Deliverable
Location entity persisted in PostgreSQL with indexes for device-id and timestamp queries.
