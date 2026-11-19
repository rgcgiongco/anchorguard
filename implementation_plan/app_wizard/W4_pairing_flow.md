# Module W4: Pairing Flow

## Goal
Display the device's pairing code to the user and optionally allow immediate pairing if the user has signed in as parent on this device.

## Context
- Child device already registered (from W2) → has deviceId and pairingCode
- Parent can sign in on this device → enter pairing code to bind immediately
- Or parent can pair from another device/web console using this tablet's code
- Pairing code is single-use and expires

## Tasks

### W4.1: Create WizardPairingScreen.kt
- [ ] File: `app/src/main/java/com/anchorguard/client/ui/screens/wizard/WizardPairingScreen.kt`
- [ ] Display pairing code prominently (large text, copy-friendly)
- [ ] Show expiry time remaining
- [ ] "Refresh Code" button → calls `AuthRepository.refreshPairingCode()`
- [ ] Section: "Complete pairing on this device" (only if parent session exists)
  - Pairing code input (6 chars)
  - "Pair device" button → calls `AuthRepository.pairDevice()`
- [ ] Section: "Or pair from another device"
  - Instruction text: "Sign in as parent on your phone or web console and enter this code"
- [ ] "Skip for now" button → proceed to safety options (pairing can be completed later)

### W4.2: Pairing State Management
- [ ] Track `device_paired` flag in SecureSessionStore
- [ ] If already paired (from previous setup), skip this screen

## Deliverable
- Pairing code displayed clearly on child device
- Option to pair immediately if parent signed in on this device
- Option to defer pairing and complete from another device
- Skip option with warning that enforcement won't work until paired

## Files to Create
- `app/src/main/java/com/anchorguard/client/ui/screens/wizard/WizardPairingScreen.kt`

## Files to Modify
- `app/src/main/java/com/anchorguard/client/auth/security/SecureSessionStore.kt` — ensure `isDevicePaired()` flag

## Test Scenario
1. Child device registered → WizardPairingScreen shows code "ABC123"
2. User taps "Refresh Code" → new code generated
3. User signs in as parent → enters code → taps "Pair device" → device paired
4. Or user taps "Skip for now" → proceeds to safety options (with warning)
