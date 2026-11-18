# Module 01 — Android Mobile Client (Phone + Tablet)

## Form Factors
- **Phone:** Portrait-only, single-column layout, bottom sheet/CTA bar.
- **Tablet:** Landscape-optimized, multi-column settings hub, master-detail panes where beneficial (e.g., app list left + policy editor right).
- Responsive breakpoints based on smallest width (`sw600dp` for tablet).
- Do not replicate the reference's 316 px purple gradient app bar or column-major settings grid. Design an original navigation shell (rail on tablet, bottom navigation or drawer on phone).

## Core Screens (original design, not 1:1)
| Screen | Purpose |
|---|---|
| Splash / Branding | AnchorGuard wordmark, version, legal links |
| Onboarding | Role selection (parent device vs child device), age band, device owner mode |
| Parent Gate | PIN entry (masked), biometric fallback, "Forgot PIN" reset flow |
| Child Home | App launcher grid, remaining time indicator, wallpaper |
| Parent Dashboard | Device list, quick actions, subscription status |
| Device Settings | App allow-list, per-app time limits, schedules, internet toggle |
| User Management | Add/switch users, optional per-user PIN |
| Timer | Temporary lock, presets (5/15/30 min, 1 hour), custom minutes |
| Security | Device Admin status, tamper proofing, boot start, keep-alive |
| UI Settings | Icon size, font size, theme, category view |
| Backup / Restore | Local backup, restore from file, cloud sync status |
| Account | Parent email, password change, linked devices, logout |

## Navigation Model
- Phone: bottom navigation (Home, Devices, Settings) or drawer; back arrow for detail pages.
- Tablet: persistent left rail (navigation drawer) + detail pane; no hamburger menu.
- No replicated sidebar group labels from the reference ("Kids Place", "Safe Browser", "General"). Use original group names (e.g., "Device Control", "Screen Time", "Users", "Security").

## Design System (original)
- **Brand:** AnchorGuard brand colour (e.g., deep teal `#0F6B6B` or slate blue `#3F51B5` — choose one, do not reuse `#5C49E0` or `#4E73DF`).
- **Typography:** System sans or Google Font (Inter / Roboto), original scale.
- **Components:** Primary button, secondary button, toggle, chip, radio card, time picker, week grid, dialog, empty state, snackbar.
- **Dark/Light:** System-driven theme, single source of truth (not per-screen dark/light split like the reference).

## Jetpack Compose Structure
```
ui/
  theme/          Color, Type, Shape tokens
  components/     Reusable buttons, toggles, cards, chips
  screens/        One Compose file per screen
  nav/            NavHost, routes, deep links (optional)
```

## Dependencies (Version Catalog)
- `androidx.compose:compose-bom`
- `androidx.room:room-runtime`, `room-ktx`
- `androidx.hilt:hilt-navigation-compose`
- `androidx.work:work-runtime-ktx`
- `com.google.dagger:hilt-android`
- `org.jetbrains.kotlinx:kotlinx-coroutines-android`
- `com.squareup.retrofit2:retrofit`, `converter-gson`, `coroutines`
- `com.squareup.okhttp3:okhttp`, `logging-interceptor`

## Build Configuration
- `minSdk 29` (Android 10), `targetSdk 36`, `compileSdk 36`
- No native libraries (pure Kotlin/Java like reference)
- R8 obfuscation enabled for release (your own mapping, do not reuse reference mapping)
- Signing config: new keystore, your own alias
