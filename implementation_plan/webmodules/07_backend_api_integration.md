# Module 7: Backend API Integration

## Goal
Connect frontend to AnchorGuard backend API.

## Tasks
1. Create `src/lib/api.js` — axios/fetch wrapper with base URL `https://anchorguard.tail78464.ts.net/api/v1/`
2. Create `src/hooks/useAuth.js` — login, signup, token storage (localStorage), logout
3. Create `src/hooks/useDevices.js` — fetch devices, register device, delete device
4. Create `src/hooks/usePolicies.js` — fetch policies, update app block, screen time
5. Create `src/hooks/useReports.js` — fetch usage data, export reports
6. Add auth protected routes wrapper
7. Add loading and error states for all API calls
8. Verify CORS works with `https://anchorguard.tail78464.ts.net`

## Deliverable
Frontend connected to live backend API