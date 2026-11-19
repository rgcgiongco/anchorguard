# Module 11: Web Console Location Route

## Goal
Wire up the `/locations` route in the web console router.

## Tasks
1. Import `LocationPage` in `src/App.tsx`
2. Add route:
   ```tsx
   <Route path="/locations" element={<RequireAuth><LocationPage /></RequireAuth>} />
   ```
   - Protected route: requires authentication (parent only)
   - Placed alongside existing `/dashboard` routes
3. Add navigation link to Location page in:
   - `src/components/layout/Navbar.tsx` (public nav)
   - `src/components/layout/Sidebar.tsx` (dashboard sidebar, under Reports or Security)
4. Add location icon to sidebar nav items
5. Verify route renders without errors at `/locations`
6. Verify `tsc --noEmit` passes

## Deliverable
`/locations` route accessible to authenticated parents, linked from navigation.
