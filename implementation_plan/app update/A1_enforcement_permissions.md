# Module A1: Enforcement Permission Guide (Device Admin, Accessibility, Overlay)

## Goal
Guide the user through granting the three critical Android enforcement permissions on first launch of the child device, and verify they are active before allowing access to the dashboard.

## Context
- AndroidManifest.xml already declares: Device Admin, Accessibility, SYSTEM_ALERT_WINDOW, FOREGROUND_SERVICE, etc.
- DeviceSettingsScreen.kt currently only has Location tracking toggle — no enforcement permission checks
- No UI exists to guide user through granting these permissions
- No code verifies permissions are actually granted before enforcement starts

## Tasks

### A1.1: Permission Check Utilities
- [ ] Create `EnforcementPermissionChecker.kt` in `auth/security/` or `enforcement/`
- [ ] `isDeviceAdminActive()`: Check via `DevicePolicyManager.isAdminActive(componentName)`
- [ ] `isAccessibilityServiceEnabled()`: Check via `Settings.Secure.getInt(..., ENABLED_ACCESSIBILITY_SERVICES)`
- [ ] `isOverlayPermissionGranted()`: Check via `Settings.canDrawOverlays(context)`
- [ ] `isBatteryOptimizationExempt()`: Check via `PowerManager.isIgnoringBatteryOptimizations(packageName)`

### A1.2: Permission Guide Screen
- [ ] Create `PermissionGuideScreen.kt` in `ui/screens/`
- [ ] Show three cards: Device Admin, Accessibility Service, Overlay Windows
- [ ] Each card shows: status (granted/pending), description of why needed, "Grant" button
- [ ] "Grant" button launches appropriate system intent:
  - Device Admin: `ACTION_ADD_DEVICE_ADMIN` with `ComponentName`
  - Accessibility: `Settings.ACTION_ACCESSIBILITY_SETTINGS`
  - Overlay: `Settings.ACTION_MANAGE_OVERLAY_PERMISSION` with package URI
- [ ] Battery optimization: `Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` with package URI

### A1.3: Splash Screen Integration
- [ ] Update SplashScreen to check all 4 permissions on launch
- [ ] If all granted → proceed to ChildHome / ParentDashboard
- [ ] If any missing → route to PermissionGuideScreen
- [ ] After user returns from granting permissions, re-check and proceed if all granted

### A1.4: Enforcement Blocker
- [ ] If permissions not granted, show warning on ParentDashboard: "Enforcement permissions not granted — app blocking will not work"
- [ ] Do not block access to dashboard, but clearly indicate enforcement is inactive

## Deliverable
- PermissionGuideScreen with 3-4 permission cards
- Permission check utilities
- Splash screen routes to PermissionGuide if permissions missing
- Dashboard warning when enforcement is inactive

## Files to Create/Modify
- Create: `app/src/main/java/com/anchorguard/client/enforcement/PermissionChecker.kt`
- Create: `app/src/main/java/com/anchorguard/client/ui/screens/PermissionGuideScreen.kt`
- Modify: `app/src/main/java/com/anchorguard/client/ui/screens/SplashScreen.kt`
- Modify: `app/src/main/java/com/anchorguard/client/ui/screens/ParentDashboardScreen.kt` (add warning)

## Test Scenario
1. Fresh install on child device → launch app → Splash → PermissionGuide (all 4 missing)
2. Grant Device Admin → return → PermissionGuide shows Device Admin granted, others pending
3. Grant all 4 → return → Splash → ChildHome / ParentDashboard
4. ParentDashboard shows no enforcement warning
