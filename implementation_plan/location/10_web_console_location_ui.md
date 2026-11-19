# Module 10: Web Console Location UI

## Goal
Build location map page with device markers, history, and geofence visualization.

## Tasks
1. Install map library: `react-leaflet` + `leaflet` (or `@react-google-maps/api`)
2. Create `src/components/location/LocationMap.tsx`:
   - Leaflet map centered on device location
   - Device marker with popup showing accuracy and timestamp
   - Geofence zone circles (color-coded by zone)
   - Location history polyline (path trace)
   - Responsive: full height, works on mobile
3. Create `src/components/location/LocationMarker.tsx`:
   - Custom marker with device icon and status badge (online/offline)
   - Click to show location details modal
4. Create `src/components/location/LocationHistoryList.tsx`:
   - Chronological list of location points
   - Timestamp formatting (relative time: "2 hours ago")
   - Accuracy indicator per point
5. Create `src/components/location/GeofenceZoneEditor.tsx`:
   - Form to add/edit geofence zones (name, radius, map click to set center)
   - Delete zone button
6. Create `src/pages/LocationPage.tsx`:
   - Page layout with Navbar + Footer (consistent with other pages)
   - Device selector dropdown (from DeviceContext)
   - Map section (width: 70% desktop, 100% mobile)
   - History list section (width: 30% desktop, stacked mobile)
   - Geofence zone editor panel (collapsible)
   - "Enable Location Tracking" toggle (calls device settings API)
   - Empty state when no location data: `empty-activity.jpg` or similar
7. Add location-related Tailwind classes (none needed beyond existing config)
8. Verify `tsc --noEmit` passes

## Deliverable
Location page at `/locations` with interactive map, device markers, history list, geofence zones, and tracking toggle.
