# Module 09 — UX/UI Design System

## Scope
Original AnchorGuard brand, design tokens, components, and screen patterns for Android client and web parent console. No copied reference theme, colours, or layouts.

## Brand Identity (AnchorGuard)
- Product name: AnchorGuard (never Kids Place / Kiddoware / Guardify)
- Tagline (example): "Guided access, anchored in trust"
- Visual metaphor: anchor, shield, or checkmark — original mark, not copied

## Colour Palette (original, not #5C49E0 / #4E73DF)
- Primary: `#2E6F8E` (steel blue) — anchor brand
- Primary dark: `#1A4A5F`
- Secondary: `#4CAF7A` (muted green) — allow/safe state
- Danger: `#C0504D` — blocked/alert
- Warning: `#D9A441` — timer/limit approaching
- Surface: `#FFFFFF` / `#F7F9FB` (cards, backgrounds)
- Surface variant: `#E8EEF3`
- On-primary: `#FFFFFF`
- On-surface: `#1C2530`
- On-surface-variant: `#5B6B7C`
- Outline: `#B9C4CE`
- Success: `#3E8E5A`
- Error: `#C0504D`

## Typography
- Headings: 600 weight, tracking tight
- Body: 400/500 weight, 14–16 sp
- Monospace for codes/timers: system monospace, 14 sp
- All text original copy (no reference strings)

## Components (Android Compose)
- `AnchorButton` — filled primary, outlined, text variants
- `AnchorCard` — elevation, rounded corners (12dp), surface bg
- `AnchorTextField` — outlined, label, leading icon, error state
- `AnchorSwitch` — primary toggle
- `AnchorChip` — filter/category chip, selected state
- `AnchorListItem` — leading avatar/icon, title, subtitle, trailing action
- `AnchorBottomNav` — 3–5 destinations, active/inactive tint
- `AnchorTopAppBar` — title, navigation icon, action icons
- `AnchorSnackbar` — success/error/warning variants
- `AnchorDialog` — confirm/destructive/alert with primary + secondary actions
- `AnchorProgress` — circular/linear indeterminate
- `AnchorBadge` — count badge on nav/icon

## Design Tokens (Compose Material 3-based, custom)
- `AnchorColorScheme` — primary, secondary, surface, error, etc.
- `AnchorTypography` — h1–h6, bodyLarge/Medium/Small, label
- `AnchorShapes` — small (4dp), medium (8dp), large (12dp), full
- `AnchorElevation` — level 0–4

## Screens & Layouts (Android)
- Splash — logo + "AnchorGuard" wordmark, animated fade
- Welcome — role selection (Parent / Child)
- Auth — register/login email+password
- PinCreate — masked PIN entry + confirm (>=4 digits)
- PinVerify — masked PIN entry for parent gate
- Pairing — 6-char code input
- ChildHome — app list with blocked/allowed badges, timer
- ParentGate — PIN/biometric unlock before exiting parent mode
- ParentDashboard — device overview, usage summary, quick actions
- DeviceSettings — per-device policies, app rules, schedule
- Security — Device Admin, overlay, usage access, accessibility, battery
- Users — child profiles per device
- Account — profile, change password, logout, delete

## Web Console Design System (React/Vue)
- Shell: fixed sidebar (left), top header (right), content canvas
- Sidebar: logo, nav items (Dashboard, Devices, Users, Security, Reports, Account)
- Header: device switcher (persistent), user profile, notifications
- Content: page title, breadcrumb, card-based layout
- Data table: sortable, filterable, pagination, empty state
- Device card: name, status, last seen, app count, blocked count, quick actions
- Form patterns: validation inline, submit states, success/error toasts

## Interaction Patterns
- Masked PIN entry (dots, never show digits)
- Confirmation dialogs for destructive actions (delete, unpair, factory reset)
- Toast/snackbar for feedback (auto-dismiss 4s, actionable)
- Loading skeletons instead of bare spinners where possible
- Empty states with illustration + primary action

## Clean Room Rules
- All colour tokens, component names, screen names original AnchorGuard names
- No reference palette (#5C49E0, #4E73DF, 316px gradient app bar)
- No copied layout structure (column-major settings grid, etc.)
- No reference strings or asset names

## Deliverables
- `ui/theme/AnchorTheme.kt` (Compose)
- `ui/components/` (AnchorButton, AnchorCard, etc.)
- `ui/screens/` (all screens with original layouts)
- `web-console/src/theme/` (colours, typography, components)
- `web-console/src/components/` (shell, data table, device card, forms)
- Design tokens documented in this module spec
