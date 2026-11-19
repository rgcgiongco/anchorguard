# Module 10: Deployment

## Goal
Publish website via Tailscale Funnel.

## Tasks
1. Build production: `npm run build` → `dist/` folder
2. Configure SPA server (`/tmp/kilo/spa_server.py`) to serve `dist/` on port 8080
3. Tailscale Funnel exposes port 8080 at `https://anchorguard.tail78464.ts.net/`
4. Verify site loads: homepage, features, console, login, signup, pricing
5. Set up automatic rebuild on git push (GitHub Actions or manual)

## Deliverable
Live website at `https://anchorguard.tail78464.ts.net/`