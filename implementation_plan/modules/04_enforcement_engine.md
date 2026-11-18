# Module 04 — Enforcement Engine (Android)

## Philosophy
AnchorGuard enforces policy on the child device locally so it works offline. The cloud provides configuration; the client enforces it.

## Components

### 4.1 AnchorGuardService (Foreground Service)
- Declared `foregroundServiceType="specialUse"` (or equivalent for your target Android versions)
- Persistent notification (required by Android for long-running services)
- Watchdog loop: polls app usage, enforces blocks, shows overlay
- `START_REDELIVER_INTENT` so system restarts it if killed
- Silent notification channel (no sound, importance LOW)

### 4.2 App Blocker / Overlay
- `SYSTEM_ALERT_WINDOW` permission to draw block overlay on top of blocked apps
- Inflate an original overlay layout (not `R.layout.block_popup` from reference)
- Overlay shows: "App blocked by AnchorGuard", app icon, unlock button (parent gate), timer
- Polling interval configurable (default 500 ms; choose your own)

### 4.3 Device Admin (DevicePolicyManager)
- `AnchorGuardDeviceAdminReceiver` handles `DEVICE_ADMIN_ENABLED`, password change, lock now
- Prevents uninstall when admin active (tamper proofing)
- Can lock screen, set password policies

### 4.4 AccessibilityService (optional, deferred or included)
- `BIND_ACCESSIBILITY_SERVICE`
- Decide if you need event-driven blocking or stick to polling watchdog
- If used: monitor window content changes, package name transitions

### 4.5 Boot Persistence
- `RECEIVE_BOOT_COMPLETED` receiver
- Starts AnchorGuardService on boot
- Custom action receiver for restart triggers

### 4.6 Package Observation
- `QUERY_ALL_PACKAGES` to list installed apps
- `PackageObserver` or `PackageManager` callbacks to detect new installs
- Auto-approve / auto-block new apps based on policy

### 4.7 Enforcement State Machine
States:
- `IDLE` — service stopped
- `RUNNING` — watchdog active, monitoring
- `BLOCKED` — overlay showing, app blocked
- `PARENT_MODE` — parent gate unlocked, full access

Transitions driven by screen on/off, device unlock, package launches, timer expiry.

## Permissions (AndroidManifest)
```
QUERY_ALL_PACKAGES
PACKAGE_USAGE_STATS
GET_TASKS
KILL_BACKGROUND_PROCESSES
RESTART_PACKAGES
RECEIVE_BOOT_COMPLETED
SYSTEM_ALERT_WINDOW
FOREGROUND_SERVICE
SPECIAL_USE_APP_ACCESS
WAKE_LOCK
BIND_DEVICE_ADMIN
BIND_ACCESSIBILITY_SERVICE
```

## Clean Room Rules
- All class names original: `AnchorGuardService`, `AnchorGuardDeviceAdminReceiver`, `AnchorGuardAccessibilityService`, `BlockOverlayView`, `AppWatchdog`, `PolicyEngine`
- Do not reference obfuscated reference class names (`Utility`, `com.kiddoware.kidsplace.i`, `ud.*`, `l0`, etc.)
- Overlay layout original XML, not `block_popup`

## Testing Enforcement
- Emulator tests: install app, enable Device Admin, grant overlay, launch blocked app, verify overlay appears
- Battery optimization whitelist request (`REQUEST_IGNORE_BATTERY_OPTIMIZATIONS`)
