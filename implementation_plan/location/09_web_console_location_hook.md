# Module 9: Web Console Location Hook

## Goal
Create React hook for location data with loading and error states.

## Tasks
1. Create `src/hooks/useLocations.ts`:
   - `useLocations(deviceId: string, params?: LocationHistoryParams)`:
     - Returns `{ locations: LocationPoint[]; loading: boolean; error: string | null; refetch: () => void }`
     - Uses `useQuery` from @tanstack/react-query with queryKey `['locations', deviceId, params]`
   - `useCurrentLocation(deviceId: string)`:
     - Returns `{ location: LocationPoint | null; loading: boolean; error: string | null }`
   - `useGeofenceZones(deviceId: string)`:
     - Returns `{ zones: GeofenceZone[]; loading: boolean; error: string | null }`
   - `useCreateGeofenceZone(deviceId: string)`:
     - Returns mutation for creating geofence zones
   - `useDeleteGeofenceZone()`:
     - Returns mutation for deleting geofence zones
2. All hooks wrap `locationsApi` with loading/error states and Toast notifications
3. Verify `tsc --noEmit` passes

## Deliverable
React hooks for location data with React Query caching, loading/error states, and Toast notifications.
