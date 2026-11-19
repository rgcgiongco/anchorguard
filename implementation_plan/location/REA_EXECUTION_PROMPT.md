# REA Execution Prompt: AnchorGuard Location (GPS Tracking) Implementation

## Context
Project: AnchorGuard — Android phone/tablet client + web parent console + self-hosted Spring Boot backend
Android client: Kotlin + Jetpack Compose at `/home/anchor/AnchorGuard/app/`
Backend API: Spring Boot at `/home/anchor/AnchorGuard/backend/` — running at `https://anchorguard.tail78464.ts.net/api/v1/`
Web console: React + Vite + Tailwind CSS at `/home/anchor/AnchorGuard/web-console/`
Tailscale Funnel: `https://anchorguard.tail78464.ts.net/` (exposing port 8080)
Brand colors: Deep Navy `#1E3A8A`, Safety Orange `#F97316`, White `#FFFFFF`

## Modules to Execute (in order)
Module files are at `/home/anchor/AnchorGuard/implementation_plan/location/`:
- `00_project_foundation.md`
- `01_android_location_client.md`
- `02_android_location_database.md`
- `03_android_location_sync.md`
- `04_backend_location_entity.md`
- `05_backend_location_api.md`
- `06_backend_location_service.md`
- `07_web_console_location_types.md`
- `08_web_console_location_api.md`
- `09_web_console_location_hook.md`
- `10_web_console_location_ui.md`
- `11_web_console_location_route.md`
- `12_testing_qa.md`

## Instructions
1. Read each module file in order
2. Reverse engineer the existing project structure (if any)
3. Implement all tasks in each module before moving to the next
4. Verify each module completes successfully before proceeding
5. Follow existing code conventions: Kotlin for Android/Spring Boot, TypeScript for web console
6. Ensure location permissions are requested at runtime on Android
7. Connect to the live backend API at `https://anchorguard.tail78464.ts.net/api/v1/`
8. Battery-conscious: use balanced power accuracy for background tracking
9. Privacy-first: clear UI disclosure, user consent, ability to disable tracking

## Deliverable
GPS location tracking implemented across all three layers:
- Android client: location capture, foreground service, local Room storage, background sync
- Backend: Location entity, REST API endpoints, geofence evaluation
- Web console: location map view, device markers, location history, geofence visualization
