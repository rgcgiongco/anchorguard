# Module 09 — Testing & QA

## Goal
Verify the dashboard end-to-end with Playwright E2E tests on Chromium.

## Tasks
1. Install Playwright with Chromium browser
2. Create `tests/dashboard.e2e.spec.ts` with 6 test cases:
   - Unauthenticated access redirects to login
   - Login redirects to dashboard and renders all sections
   - Sidebar navigation contains all required items
   - Device status empty state renders when no devices linked
   - Toggle switches in app blocking are interactive
   - Sign out works from sidebar
3. Configure `playwright.config.ts` with Chromium project
4. Use browser-side fetch for authentication (same-origin, avoids CORS)
5. Use `waitUntil: 'commit'` for page navigation
6. Run tests: `npx playwright test --project chromium --workers=1`
7. All 6 tests must pass

## Files
- `tests/dashboard.e2e.spec.ts` — E2E test suite
- `playwright.config.ts` — Playwright configuration

## Deliverable
All 6 E2E tests pass on Chromium against live site at `https://anchorguard.tail78464.ts.net/dashboard`.
