# Module W2: Parent Auth / Child Auto-Registration

## Goal
Parent users sign in or create an account. Child devices auto-register with the backend to generate a device ID and pairing code.

## Context
- Parent path: reuses existing `AuthRepository.register()` / `login()` logic
- Child path: calls `AuthRepository.registerDevice()` automatically (no user input)
- Both paths store tokens/device credentials in `SecureSessionStore`

## Tasks

### W2.1: Parent Authentication (WizardParentAuthScreen.kt)
- [ ] File: `app/src/main/java/com/anchorguard/client/ui/screens/wizard/WizardParentAuthScreen.kt`
- [ ] Email input field
- [ ] Password input field
- [ ] "Sign In" button → calls `AuthRepository.login()`
- [ ] "Create Account" button → calls `AuthRepository.register()`
- [ ] On success → navigate to WizardPermissions (skip to W3 if parent device already has permissions)
- [ ] Error handling: invalid email/password, network errors

### W2.2: Child Auto-Registration (WizardChildRegisterScreen.kt)
- [ ] File: `app/src/main/java/com/anchorguard/client/ui/screens/wizard/WizardChildRegisterScreen.kt`
- [ ] Auto-calls `AuthRepository.registerDevice()` on screen load
- [ ] Shows "Registering this device..." spinner
- [ ] On success: stores deviceId, deviceApiKey in SecureSessionStore
- [ ] Generates pairing code → display on next screen (WizardPairing)
- [ ] On error: show "Registration failed" with retry button

### W2.3: Update SecureSessionStore for Wizard State
- [ ] Add `setupWizardCompleted()` / `isSetupWizardCompleted()` flags
- [ ] Add `deviceRegistered()` / `devicePaired()` helpers if not existing
- [ ] Persist wizard step completion

## Deliverable
- Parent can sign in or create account during setup
- Child device auto-registers with backend and gets device ID + pairing code
- Credentials stored securely in EncryptedSharedPreferences

## Files to Create
- `app/src/main/java/com/anchorguard/client/ui/screens/wizard/WizardParentAuthScreen.kt`
- `app/src/main/java/com/anchorguard/client/ui/screens/wizard/WizardChildRegisterScreen.kt`

## Files to Modify
- `app/src/main/java/com/anchorguard/client/auth/security/SecureSessionStore.kt` — add wizard state flags
- `app/src/main/java/com/anchorguard/client/auth/data/AuthRepository.kt` — may need helper for registration status

## Test Scenario
1. Parent path: enter email/password → sign in → JWT stored → proceed to permissions
2. Child path: auto-register → deviceId stored → pairing code generated → proceed to pairing screen
