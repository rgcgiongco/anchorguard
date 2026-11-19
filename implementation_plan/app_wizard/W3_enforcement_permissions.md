# Module W3: Enforcement Permission Guide

## Goal
Guide the child device user through granting the four critical Android enforcement permissions: Device Admin, Accessibility Service, Overlay Windows, and Battery Optimization exclusion.

## Context
- Device Admin: `DevicePolicyManager.isAdminActive()`
- Accessibility: `Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES`
- Overlay Windows: `Settings.canDrawOverlays()`
- Battery Optimization: `PowerManager.isIgnoringBatteryOptimizations()`
- Already partially covered in Module A1 — this module integrates it into the wizard flow

## Tasks

### W3.1: Create WizardPermissionGuideScreen.kt
- [ ] File: `app/src/main/java/com/anchorguard/client/ui/screens/wizard/WizardPermissionGuideScreen.kt`
- [ ] Four permission cards, each with:
  - Icon
  - Title (e.g., "Device Admin")
  - Description (why it's needed)
  - Status chip: "Granted" (green) or "Pending" (red)
  - "Grant" button → launches system intent
- [ ] Cards update status in real-time as user returns from system settings
- [ ] Bottom button: "I've granted all permissions" (enabled only when all 4 granted)
- [ ] Individual grant intents:
  - Device Admin: `ACTION_ADD_DEVICE_ADMIN` with `ComponentName`
  - Accessibility: `Settings.ACTION_ACCESSIBILITY_SETTINGS`
  - Overlay: `Settings.ACTION_MANAGE_OVERLAY_PERMISSION` with `package:` URI
  - Battery Optimization: `Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` with `package:` URI

### W3.2: Create Permission Check Utilities (if not in Module A1)
- [ ] `EnforcementPermissionChecker.kt` with:
  - `isDeviceAdminActive(context): Boolean`
  - `isAccessibilityServiceEnabled(context): Boolean`
  - `isOverlayPermissionGranted(context): Boolean`
  - `isBatteryOptimizationExempt(context): Boolean`

### W3.3: Persist Permission State
- [ ] Save each permission grant in `SecureSessionStore` or `SyncPreferences`
- [ ] Track: `device_admin_granted`, `accessibility_granted`, `overlay_granted`, `battery_opt_out`

## Deliverable
- User can grant all 4 enforcement permissions through guided wizard
- System intents launch correctly for each permission
- Status updates when user returns from settings
- Cannot proceed until all 4 are granted (enforcement requirement)

## Files to Create
- `app/src/main/java/com/anchorguard/client/ui/screens/wizard/WizardPermissionGuideScreen.kt`
- `app/src/main/java/com/anchorguard/client/enforcement/EnforcementPermissionChecker.kt`

## Files to Modify
- `app/src/main/java/com/anchorguard/client/auth/security/SecureSessionStore.kt` — add permission state flags

## Test Scenario
1. Child device → WizardPermissionGuide → all 4 show "Pending"
2. Tap "Grant" on Device Admin → system dialog → grant → return → card shows "Granted"
3. Repeat for Accessibility, Overlay, Battery Optimization
4. All 4 granted → "I've granted all permissions" button enabled → proceed to pairing
