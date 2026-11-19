# REA Execution Prompt: AnchorGuard First-Time Setup Wizard

## Context
Project: AnchorGuard — Android phone/tablet client + web parent console + self-hosted Spring Boot backend
Android app: Native Kotlin + Jetpack Compose at `/home/anchor/AnchorGuard/app/`
Backend API: `https://anchorguard.tail78464.ts.net/api/v1/` (live)

## Problem
The Android app currently launches directly into Welcome/RoleSelection screens with no guided first-time setup. Critical enforcement permissions (Device Admin, Accessibility, Overlay, Battery Optimization) are not guided, and there's no structured onboarding flow for device registration, pairing, and safety configuration.

## Goal
Create a multi-step first-time setup wizard that guides users through:
1. Welcome & role selection (Parent vs Child)
2. Parent sign-in OR child auto-registration
3. Enforcement permission guidance (4 permissions)
4. Pairing code display + optional immediate pairing
5. Optional safety options (PIN, SOS, geofence)
6. Completion + dashboard routing

## Modules to Execute
Module files at `/home/anchor/AnchorGuard/implementation_plan/app_wizard/`:
- `W1_welcome_role_selection.md`
- `W2_parent_auth_child_register.md`
- `W3_enforcement_permissions.md`
- `W4_pairing_flow.md`
- `W5_safety_options.md`
- `W6_completion_dashboard.md`

## Execution Order (strict)
W1 → W2 → W3 → W4 → W5 → W6

## Instructions
1. Read each module file in order
2. Reverse engineer existing codebase structure:
   - `SplashScreen.kt` — current 800ms delay + direct route to Welcome
   - `RoleSelectionScreen.kt` — existing parent/child choice
   - `AuthRepository.kt` — `login()`, `register()`, `registerDevice()`, `pairDevice()`
   - `SecureSessionStore.kt` — token/device credential storage
   - `AnchorGuardNavHost.kt` — navigation routes
   - `DeviceSettingsScreen.kt` — current permission/settings UI
3. Implement W1 first (welcome + role selection + nav host integration)
4. Implement W2 (parent auth + child auto-registration)
5. Implement W3 (enforcement permissions — integrates with Module A1)
6. Implement W4 (pairing flow)
7. Implement W5 (safety options — integrates with Modules A6/A7)
8. Implement W6 (completion + dashboard routing)
9. Verify wizard doesn't re-show on subsequent launches (setup_wizard_completed flag)

## Deliverable
Complete first-time setup wizard with:
- Welcome + role selection
- Parent sign-in / child auto-registration
- Enforcement permission guide (4 permissions)
- Pairing code display + optional immediate pairing
- Optional PIN, SOS, geofence setup
- Completion screen with dashboard routing
- Wizard completion flag (won't re-show on future launches)

## Test Credentials
| Account | Email | Password |
|---------|-------|----------|
| Admin | `anchor_admin@anchorguard.com` | `Gabby01172020!!!` |
| Parent | `parent@anchorguard.com` | `TestParent123!` |

## Notes
- Do NOT commit the signing keystore (`anchorguard-release.jks`) — it's not in the repo
- Test on a physical Android device (emulator cannot properly test Device Admin, Accessibility, overlay)
- All wizard screens should use consistent `ScreenScaffold` pattern from existing code
- Wizard state stored in `SecureSessionStore` (encrypted SharedPreferences)
