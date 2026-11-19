# Module W6: Completion & Dashboard Routing

## Goal
Final wizard step — mark setup as complete and route to the appropriate dashboard based on device role.

## Context
- All previous wizard steps completed
- `setup_wizard_completed` flag must be set to true
- Route based on role: Parent → ParentDashboard, Child → ChildHome

## Tasks

### W6.1: Create WizardCompleteScreen.kt
- [ ] File: `app/src/main/java/com/anchorguard/client/ui/screens/wizard/WizardCompleteScreen.kt`
- [ ] "Setup complete!" message
- [ ] Summary of what was configured:
  - Device registered: ✅
  - Permissions granted: ✅ (or ⚠️ if skipped)
  - Paired: ✅ (or ⚠️ if pending)
  - PIN set: ✅ (or ⚪ if skipped)
  - SOS enabled: ✅ (or ⚪ if skipped)
- [ ] Primary button: "Finish" → navigate to dashboard
- [ ] Secondary option: "Review settings" → back to previous wizard screen

### W6.2: Dashboard Routing Logic
- [ ] If parent role → navigate to `Routes.ParentDashboard`
- [ ] If child role → navigate to `Routes.ChildHome`
- [ ] Clear wizard back stack so user can't return to wizard

### W6.3: Persist Wizard Completion
- [ ] Call `SecureSessionStore.setupWizardCompleted(true)`
- [ ] On future launches, SplashScreen checks this flag and skips wizard

### W6.4: Update SplashScreen Logic
- [ ] On app launch: check `isSetupWizardCompleted()`
- [ ] If false → WizardWelcomeScreen
- [ ] If true → check device state → ChildHome or ParentDashboard

## Deliverable
- Wizard completion screen with setup summary
- "Finish" button routes to correct dashboard
- Wizard marked complete — won't show again on future launches

## Files to Create
- `app/src/main/java/com/anchorguard/client/ui/screens/wizard/WizardCompleteScreen.kt`

## Files to Modify
- `app/src/main/java/com/anchorguard/client/ui/screens/SplashScreen.kt` — wizard completion check
- `app/src/main/java/com/anchorguard/client/auth/security/SecureSessionStore.kt` — `setupWizardCompleted()` flag
- `app/src/main/java/com/anchorguard/client/ui/nav/AnchorGuardNavHost.kt` — wizard route entries

## Test Scenario
1. Complete all wizard steps → WizardCompleteScreen shows summary
2. Tap "Finish" → routed to ParentDashboard (if parent) or ChildHome (if child)
3. Close app → relaunch → Splash → directly to dashboard (wizard skipped)
4. Reset app data → wizard shows again on next launch
