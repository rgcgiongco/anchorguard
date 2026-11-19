# Module 10 — Deployment

## Goal
Publish the redesigned dashboard via Tailscale Funnel on port 8080.

## Tasks
1. Build production: `npm run build` → `dist/` folder
2. Start SPA server: `npx vite preview --port 8080 --host` serving `dist/`
3. Configure Tailscale Funnel: `tailscale funnel 8080`
4. Funnel exposes port 8080 at `https://anchorguard.tail78464.ts.net/`
5. Verify site loads: dashboard at `https://anchorguard.tail78464.ts.net/dashboard`
6. Verify login with test credentials: `parent@anchorguard.com` / `TestParent123!`
7. Verify all dashboard sections render on live site
8. Set up automatic rebuild on changes (manual or GitHub Actions)

## Deliverable
Live website at `https://anchorguard.tail78464.ts.net/dashboard` showing redesigned parent dashboard.
