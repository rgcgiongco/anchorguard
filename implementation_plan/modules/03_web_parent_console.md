# Module 03 — Web Parent Console (Management Only)

## Scope
Desktop-first single-page web application for parents to manage child devices. Mobile-responsive but optimized for desktop. Management only — no child-facing features.

## Tech Stack
- TypeScript
- React or Vue 3 (choose one)
- State: TanStack Query (React) or Pinia + useFetch (Vue)
- HTTP client: Axios or fetch wrapper with JWT interceptor
- Charts (deferred): Chart.js or Recharts
- Build: Vite

## Shell Layout (original design)
- **Header:** AnchorGuard logo (original mark), notification bell, parent avatar/menu.
- **Sidebar:** Navigation groups with original labels:
  - Dashboard
  - Devices
  - Screen Time
  - Users
  - Security
  - Reports
  - Account
- **Content canvas:** White cards, 24 px spacing, max-width constraint (~1100 px) for readability (avoid reference's 2300 px line length).
- **Footer:** Brand credit, version, legal links.

## Pages
| Page | Purpose |
|---|---|
| Dashboard | Device overview, online status, quick actions, subscription |
| Device Detail | Per-device settings, app policy, schedule editor |
| App Policy | Allow/block apps, set time limits, internet toggle |
| Schedule | Weekly grid editor with presets (School, Bedtime, Custom) |
| Users | Manage child profiles, per-user PIN |
| Security | Device Admin status, tamper proofing, boot start |
| Reports | Usage tables, top apps, screen time trends |
| Account | Email, password, billing (deferred), logout |

## Interaction Patterns (original)
- **Device switcher:** Persistent selector in header (not hidden in title bar).
- **Save model:** Explicit "Save" per page with "Saved" toast; dirty-state warning on navigation.
- **Destructive actions:** Typed confirmation ("type DELETE to confirm") or undo toast; no bare `?` suffixes.
- **Empty states:** Illustration + copy + primary CTA, centered.
- **Modals:** Centered card, scrim, Cancel primary-right, Cancel left.
- **No breadcrumbs** (avoid replicating reference IA problems); use clear page titles and active sidebar highlight.

## Responsive Breakpoints
- Desktop: ≥ 1024 px (sidebar + content)
- Tablet: 640–1023 px (sidebar collapses to icons or top bar)
- Mobile: < 640 px (bottom nav or drawer, stacked cards)

## Authentication
- JWT stored in httpOnly cookie or memory (not localStorage, to avoid XSS)
- Auto-refresh on 401
- Redirect to login on session expiry
