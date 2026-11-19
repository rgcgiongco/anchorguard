# Module 08 — Authentication & Session

## Goal
Implement parent login flow and session management for dashboard access.

## Tasks
1. Login page at `/login` with email/password form
2. Test credentials: `parent@anchorguard.com` / `TestParent123!`
3. POST `/v1/auth/login` returns access + refresh tokens
4. Store tokens in sessionStorage (`anchorguard.session.tokens`)
5. AuthProvider reads sessionStorage on mount, fetches `/v1/account/me`
6. Redirect unauthenticated users to `/login`
7. Redirect authenticated users away from `/login`
8. Sign out clears tokens and redirects to `/login`
9. Token refresh on 401 responses via axios interceptor

## Files
- `src/pages/Login.tsx` — Login page
- `src/context/AuthContext.tsx` — Auth provider with session management
- `src/auth/tokenStore.ts` — sessionStorage token persistence

## Deliverable
Login flow works with test credentials; session persists across page reloads.
