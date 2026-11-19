# Module 5: Backend Location API

## Goal
Expose REST endpoints for location ingestion and retrieval.

## Tasks
1. Create `LocationRequest.kt` — DTO for batch upload:
   - `deviceId` (UUID)
   - `locations` (List of `LocationPoint`: latitude, longitude, accuracy, timestamp)
2. Create `LocationResponse.kt` — DTO for API responses:
   - `id`, `deviceId`, `latitude`, `longitude`, `accuracy`, `timestamp`, `createdAt`
3. Create `LocationController.kt` — REST controller:
   - `POST /api/v1/locations/batch` — ingest batch from Android client
     - Auth: device-level auth (device key/header)
     - Returns 202 Accepted with count of stored locations
   - `GET /api/v1/devices/{id}/locations` — location history for a device
     - Auth: parent-level auth (JWT)
     - Query params: `from`, `to`, `limit` (default 100)
     - Returns paginated list of `LocationResponse`
   - `GET /api/v1/devices/{id}/locations/current` — latest location
     - Auth: parent-level auth (JWT)
     - Returns single `LocationResponse` or 404 if none
4. Register controller in Spring Boot component scan
5. Add API documentation (OpenAPI/Swagger) annotations
6. Test endpoints with curl/Postman against running backend

## Deliverable
REST API endpoints for location batch ingestion and parent-side retrieval.
