# Module W1: Welcome & Role Selection

## Goal
First screen of the setup wizard — welcome the user and determine if this device is for a parent or a child.

## Context
- New screen replacing direct navigation to `WelcomeScreen` / `RoleSelectionScreen`
- Runs only when `setup_wizard_completed == false` in SharedPreferences
- After completion, wizard state is saved and never shown again (unless reset)

## Tasks

### W1.1: Create WizardWelcomeScreen.kt
- [ ] File: `app/src/main/java/com/anchorguard/client/ui/screens/wizard/WizardWelcomeScreen.kt`
- [ ] Display AnchorGuard logo + tagline "Protect your family's digital world"
- [ ] Primary button: "Get Started"
- [ ] On tap → navigate to WizardRoleSelectionScreen

### W1.2: Create WizardRoleSelectionScreen.kt
- [ ] File: `app/src/main/java/com/anchorguard/client/ui/screens/wizard/WizardRoleSelectionScreen.kt`
- [ ] Two large cards:
  - "This is a Parent device" — for managing child devices
  - "This is a Child device" — for the device being monitored
- [ ] Parent card → navigate to WizardParentAuthScreen
- [ ] Child card → navigate to WizardChildRegisterScreen (auto-registration)

### W1.3: Add Wizard Routes to NavHost
- [ ] Add `Routes.WizardWelcome`, `Routes.WizardRoleSelection`, `Routes.WizardParentAuth`, `Routes.WizardChildRegister`, `Routes.WizardPermissions`, `Routes.WizardPairing`, `Routes.WizardSafety`, `Routes.WizardComplete`
- [ ] Update SplashScreen to check `setup_wizard_completed` flag
- [ ] If wizard incomplete → route to WizardWelcome instead of WelcomeScreen

## Deliverable
- Welcome screen with AnchorGuard branding
- Role selection (Parent vs Child) that branches the wizard flow
- Integration with SplashScreen to auto-launch wizard on first run

## Files to Create
- `app/src/main/java/com/anchorguard/client/ui/screens/wizard/WizardWelcomeScreen.kt`
- `app/src/main/java/com/anchorguard/client/ui/screens/wizard/WizardRoleSelectionScreen.kt`

## Files to Modify
- `app/src/main/java/com/anchorguard/client/ui/nav/AnchorGuardNavHost.kt` — add wizard routes
- `app/src/main/java/com/anchorguard/client/ui/screens/SplashScreen.kt` — check wizard flag

## Test Scenario
1. Fresh install → launch app → Splash → WizardWelcome
2. Tap "Get Started" → WizardRoleSelection
3. Pick "Parent device" or "Child device" → respective auth/register screen
