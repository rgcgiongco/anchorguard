# REA Execution Prompt: AnchorGuard Parent Dashboard Redesign

## Context
Project: AnchorGuard — Android phone/tablet client + web parent console + self-hosted Spring Boot backend
Website: React + Vite + Tailwind CSS at `/home/anchor/AnchorGuard/web-console/`
Backend API: `https://anchorguard.tail78464.ts.net/api/v1/` (already running)
Live site: `https://anchorguard.tail78464.ts.net/`

## Problem
The parent dashboard interface does not match the model design images. The current UI is too basic (empty state with "No linked devices") and lacks the comprehensive dashboard layout shown in the reference images.

## Reference Images (Model Designs)
Two model images are provided showing the target parent dashboard:
1. **Image 1** — Comprehensive parent dashboard with:
   - Top stats cards: Active Devices (3), Today's Screen Time (1h 42m), Blocked Attempts (14), Current Status (Secured)
   - App Blocking & Controls panel with toggles for Social Apps, Chat, Gaming, Learning
   - Device Status section with device cards (Leo's Tablet, Emma's Phone, Family iPad) showing app usage, screen time limits, active apps
   - Screen Time & Limits chart (bar graph with weekly/daily toggle)
   - App Usage donut chart with categories (Active Apps, Educational App, Data Filter)
   - Real-time Activity Log with timestamps and event types
   - Left sidebar navigation: Dashboard, Connected Devices, Screen Time, App Blocker, Location Tracking, Activity Logs, Web Filters, Settings

2. **Image 2** — Current state showing "No linked devices" empty state and basic sidebar

## Test Credentials for Verification
| Field | Value |
|-------|-------|
| Email | `parent@anchorguard.com` |
| Password | `TestParent123!` |
| Role | `parent` |

## What Needs to Change

### 1. Parent Dashboard Page (`/dashboard`)
Redesign to match Image 1:
- Replace the empty "No linked devices" state with the full dashboard layout
- Add top stat cards row (Active Devices, Screen Time, Blocked Attempts, Current Status)
- Add App Blocking & Controls section with toggle switches
- Add Device Status section with individual device cards showing:
  - Device name, model image placeholder
  - Live Screen Time with percentage bar
  - Active App with usage %
  - Screen Time Limit progress bar
- Add Screen Time & Limits chart (bar graph)
- Add App Usage donut chart
- Add Real-time Activity Log table
- Keep left sidebar navigation from Image 2 but add the content from Image 1

### 2. Sidebar Navigation
Match Image 2's left sidebar but ensure all items work:
- Dashboard, Devices, Screen Time, App Blocker, Location, Reports, Settings
- "No linked devices" dropdown selector at top
- User email and sign-out button in top-right

### 3. Backend API
Ensure these endpoints return data for the parent dashboard:
- `GET /v1/devices` — returns list of linked devices
- `GET /v1/devices/{id}` — returns device detail
- `GET /v1/devices/{id}/policy` — returns app blocking policy
- `GET /v1/reports/usage` — returns usage data for charts
- `GET /v1/admin/health` — returns system health (admin only)

### 4. Tech Stack
- React + Vite + Tailwind CSS
- React Router for navigation
- TanStack Query for data fetching
- Lucide React or inline SVGs for icons
- Chart library (Recharts or Chart.js) for graphs, or SVG-based sparklines

## Files to Modify
- `/home/anchor/AnchorGuard/web-console/src/pages/Dashboard.tsx` — Main parent dashboard redesign
- `/home/anchor/AnchorGuard/web-console/src/components/layout/Sidebar.tsx` — Sidebar navigation
- `/home/anchor/AnchorGuard/web-console/src/components/layout/Header.tsx` — Top header with user info
- `/home/anchor/AnchorGuard/web-console/src/components/layout/AppShell.tsx` — App shell layout
- `/home/anchor/AnchorGuard/web-console/src/hooks/useDevices.ts` — Device data fetching
- `/home/anchor/AnchorGuard/web-console/src/hooks/useReports.ts` — Usage reports data fetching
- `/home/anchor/AnchorGuard/web-console/src/api/endpoints.ts` — API endpoint definitions (if needed)

## Instructions
1. Read the existing source code at `/home/anchor/AnchorGuard/web-console/src/`
2. Analyze the two model images to understand the target design
3. Reverse engineer the current component structure
4. Implement the parent dashboard redesign matching Image 1's layout and components
5. Ensure the sidebar navigation matches Image 2's structure
6. Connect to the live backend API at `https://anchorguard.tail78464.ts.net/api/v1/`
7. Verify login works with test credentials: `parent@anchorguard.com` / `TestParent123!`
8. Build and deploy via Tailscale Funnel on port 8080

## Deliverable
Live website at `https://anchorguard.tail78464.ts.net/dashboard` showing the redesigned parent dashboard matching the model images, with working device data, charts, and toggles.
