# Module A3: Tamper Protection & Boot Persistence

## Goal
Prevent the child from uninstalling, disabling, or bypassing AnchorGuard, and ensure enforcement survives device reboots.

## Context
- Device Admin permission already requested (Module A1 covers granting it)
- `AnchorGuardDeviceAdminReceiver` already declared in AndroidManifest
- `BootCompletedReceiver` already declared in AndroidManifest
- No code actively prevents uninstall or disables admin removal
- No code ensures enforcement restarts after boot

## Tasks

### A3.1: Device Admin Protection
- [ ] Implement `onDisableRequested()` in `AnchorGuardDeviceAdminReceiver` — show warning "Disabling will stop app blocking"
- [ ] Implement `onDisabled()` — log event, attempt to re-enable if possible
- [ ] Add package installer observer to block uninstall while Device Admin active
- [ ] Add `PackageObserver` (already declared) to detect app uninstall/replace attempts

### A3.2: Boot Persistence
- [ ] `BootCompletedReceiver` on `BOOT_COMPLETED` + `LOCKED_BOOT_COMPLETED`:
  - Start `AnchorGuardService` foreground service
  - Start `AnchorGuardAccessibilityService` if accessibility granted
  - Re-check device admin status
  - Re-check overlay permission
  - Poll backend for lock state
- [ ] Add `RESTART_ENFORCEMENT` custom action handler (already in manifest)
- [ ] Add alarm manager or exact alarm to periodically restart enforcement if killed

### A3.3: Battery Optimization Exclusion
- [ ] On first launch, direct user to battery settings to exclude AnchorGuard
- [ ] Check `PowerManager.isIgnoringBatteryOptimizations()` on app resume
- [ ] Show persistent notification if battery optimization is active (warning enforcement may be killed)

### A3.4: Accessibility Service Restart
- [ ] Accessibility services can be turned off by user in settings
- [ ] Add `onUnbind()` / `onServiceDisconnected()` handler — log event, show notification to re-enable
- [ ] Use `startService()` with `START_STICKY` to attempt restart

## Deliverable
- Child cannot easily uninstall AnchorGuard without parent PIN
- Enforcement restarts after device reboot
- Battery optimization exclusion is recommended and checked
- Accessibility service auto-restart attempted if disabled

## Files to Create/Modify
- Modify: `app/src/main/java/com/anchorguard/client/enforcement/AnchorGuardDeviceAdminReceiver.kt`
- Modify: `app/src/main/java/com/anchorguard/client/enforcement/BootCompletedReceiver.kt`
- Create: `app/src/main/java/com/anchorguard/client/enforcement/BatteryOptimizationHelper.kt`
- Modify: `app/src/main/java/com/anchorguard/client/enforcement/AnchorGuardService.kt` (START_STICKY)
- Modify: `app/src/main/java/com/anchorguard/client/enforcement/AnchorGuardAccessibilityService.kt` (restart logic)
- Modify: `AndroidManifest.xml` — add exact alarm permission if needed

## Test Scenario
1. Install app, grant all permissions, reboot device
2. After boot, AnchorGuardService and AccessibilityService should be running
3. Attempt to uninstall app → blocked by Device Admin (requires parent PIN to deactivate admin first)
4. Turn off battery optimization → warning notification appears
