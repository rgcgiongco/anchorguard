# Module 7: Web Console Location Types

## Goal
Add TypeScript types for location data in the web console.

## Tasks
1. Add to `src/api/types.ts`:
   - `LocationPoint` type: `{ id: string; deviceId: string; latitude: number; longitude: number; accuracy?: number; timestamp: string; createdAt: string }`
   - `GeofenceZone` type: `{ id: string; deviceId: string; name: string; latitude: number; longitude: number; radiusMeters: number; createdAt: string }`
   - `GeofenceAlert` type: `{ id: string; deviceId: string; zoneId: string; zoneName: string; alertType: 'ENTER' | 'EXIT'; occurredAt: string }`
   - `LocationHistoryParams` type: `{ deviceId: string; from?: string; to?: string; limit?: number }`
2. Export all new types from types module
3. Verify `tsc --noEmit` passes with new types

## Deliverable
TypeScript types defined for location points, geofence zones, and alerts.
