# Module 07 — Backend API Integration

## Goal
Connect dashboard to live AnchorGuard backend API at `https://anchorguard.tail78464.ts.net/api/v1/`.

## Tasks
1. Verify `devicesApi.list()` — `GET /v1/devices` returns device list
2. Verify `policyApi.get()` — `GET /v1/devices/{id}/policy` returns app blocking policy
3. Verify `reportsApi.usage()` — `GET /v1/reports/usage` returns usage data
4. Verify `accountApi.me()` — `GET /v1/account/me` returns parent profile
5. Handle 401 responses with token refresh interceptor
6. Handle empty/error responses gracefully (empty states, fallback UI)
7. Verify CORS works from `https://anchorguard.tail78464.ts.net`

## Files
- `src/api/endpoints.ts` — devicesApi, policyApi, reportsApi, accountApi
- `src/api/client.ts` — axios instance with auth interceptor

## Deliverable
Dashboard fetches and displays data from live backend API with proper error handling.
