# REA Execution Prompt: AnchorGuard Website Implementation

## Context
Project: AnchorGuard — Android phone/tablet client + web parent console + self-hosted Spring Boot backend
Website: React + Vite + Tailwind CSS at `/home/anchor/AnchorGuard/web-console/`
Backend API: `https://anchorguard.tail78464.ts.net/api/v1/` (already running)
Tailscale Funnel: `https://anchorguard.tail78464.ts.net/` (exposing port 8080)
Images: 10 generated images at `/home/anchor/images/` — need to be copied and used
Brand colors: Deep Navy `#1E3A8A`, Safety Orange `#F97316`, White `#FFFFFF`

## Modules to Execute (in order)
Module files are at `/home/anchor/AnchorGuard/implementation_plan/webmodules/`:
- `00_project_foundation.md`
- `01_design_system_components.md`
- `02_homepage.md`
- `03_features_page.md`
- `04_dashboard_console.md`
- `05_authentication_pages.md`
- `06_dashboard_empty_states.md`
- `07_backend_api_integration.md`
- `08_pricing_page.md`
- `09_testing_polish.md`
- `10_deployment.md`

## Instructions
1. Read each module file in order
2. Reverse engineer the existing project structure (if any)
3. Implement all tasks in each module before moving to the next
4. Verify each module completes successfully before proceeding
5. Use the generated images from `/home/anchor/images/` mapped to their designated sections
6. Ensure all pages are mobile-responsive
7. Connect to the live backend API at `https://anchorguard.tail78464.ts.net/api/v1/`
8. Deploy via Tailscale Funnel on port 8080

## Deliverable
Live website at `https://anchorguard.tail78464.ts.net/` with all 7 pages:
- `/` — Homepage
- `/features` — Features
- `/console` — Dashboard marketing
- `/login` — Login
- `/signup` — Signup
- `/dashboard` — Dashboard app
- `/pricing` — Pricing