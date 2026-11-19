# Module W5: Safety Options (PIN, SOS, Geofence)

## Goal
Optional safety setup: parent PIN for device access, SOS emergency contact, and geofence safe zones.

## Context
- PIN is stored locally in SecureSessionStore
- SOS endpoint: `POST /v1/devices/{id}/sos` (from Module A7)
- Geofence endpoint: `POST /v1/geofences` (from Module A6)
- All optional — user can skip and configure later from dashboard

## Tasks

### W5.1: Create WizardSafetyOptionsScreen.kt
- [ ] File: `app/src/main/java/com/anchorguard/client/ui/screens/wizard/WizardSafetyOptionsScreen.kt`
- [ ] Three optional sections, each with toggle:
  1. **Parent PIN** — Create 4-6 digit PIN for Parent Gate access
     - Input: PIN entry, confirm PIN
     - Store in SecureSessionStore
  2. **SOS Emergency** — Enable SOS button on child device
     - Toggle switch
     - On enable: register SOS endpoint with backend
  3. **Safe Zones (Geofence)** — Set up home/school safe zones
     - Toggle switch
     - On enable: navigate to geofence setup (simple: name, radius around current location)
     - Or defer to dashboard

### W5.2: PIN Creation UI
- [ ] 4-6 digit PIN entry with visual dots
- [ ] Confirm PIN entry (must match)
- [ ] Store encrypted in SecureSessionStore
- [ ] Used for Parent Gate unlock

### W5.3: SOS Setup
- [ ] Toggle to enable SOS
- [ ] Explain: "Press and hold power button 3x or use SOS button to alert parent"
- [ ] Register device for SOS endpoint

### W5.4: Geofence Setup (Simple)
- [ ] Toggle to enable geofences
- [ ] Detect current location
- [ ] Suggest "Home" and "School" zones with radius
- [ ] User confirms or skips

### W5.5: Skip Option
- [ ] "Skip all safety options" → proceed to completion
- [ ] Warning: "You can configure these later from Parent Dashboard"

## Deliverable
- Optional PIN setup for parent gate
- Optional SOS emergency alert setup
- Optional geofence safe zone setup
- All optional with skip capability

## Files to Create
- `app/src/main/java/com/anchorguard/client/ui/screens/wizard/WizardSafetyOptionsScreen.kt`

## Files to Modify
- `app/src/main/java/com/anchorguard/client/auth/security/SecureSessionStore.kt` — store PIN
- Backend SOS/geofence endpoints (from Modules A6/A7)

## Test Scenario
1. Toggle PIN on → enter 4-digit PIN → confirm → stored
2. Toggle SOS on → registered with backend
3. Toggle Geofence on → current location detected → "Home" zone suggested
4. Tap "Complete" → proceed to WizardCompleteScreen
5. Or tap "Skip all" → warning dialog → proceed to completion
