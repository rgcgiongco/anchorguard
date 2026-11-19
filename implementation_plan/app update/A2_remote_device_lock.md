# Module A2: Remote Device Lock

## Goal
Enable the parent to remotely lock the child device from the web console or parent app, rendering it unusable until unlocked.

## Context
- DevicePolicyManager has `lockNow()` method for device admin apps
- Device Admin permission must be active (covered in Module A1)
- Backend needs a `/v1/devices/{id}/lock` endpoint
- Android app needs to receive and act on lock command

## Tasks

### A2.1: Backend - Remote Lock Endpoint
- [ ] Add `POST /v1/devices/{id}/lock` to `DeviceController.kt`
- [ ] Add `lock(deviceId)` to `DeviceService.kt` — sets `isLocked = true` in device record
- [ ] Add `unlock(deviceId)` — sets `isLocked = false`
- [ ] Add `GET /v1/devices/{id}/status` — returns `isLocked`, `lastSeen`, `isOnline`
- [ ] Verify authentication: only the parent who owns the device can lock/unlock

### A2.2: Android - Lock Command Receiver
- [ ] Create `LockCommandReceiver.kt` (BroadcastReceiver) in `enforcement/`
- [ ] Listen for custom action `com.anchorguard.client.action.REMOTE_LOCK`
- [ ] When received, call `DevicePolicyManager.lockNow()` if Device Admin active
- [ ] Show full-screen lock activity with "Unlock with parent PIN" message
- [ ] Register receiver in AndroidManifest.xml

### A2.3: Android - Lock State Polling
- [ ] Add `pollLockState()` to enforcement service — checks backend every N seconds
- [ ] If `isLocked == true`, show lock screen immediately
- [ ] If parent unlocks from backend, dismiss lock screen

### A2.4: Web Console - Lock Button
- [ ] Add "Lock Device" button to Parent Dashboard device card
- [ ] Confirm dialog: "Lock this device? Child will not be able to use it until you unlock."
- [ ] Call `POST /v1/devices/{id}/lock`

### A2.5: Android - Lock Activity
- [ ] Create `DeviceLockActivity.kt` — full-screen, cannot be dismissed without PIN
- [ ] Show parent contact info and "Unlock" button requiring PIN
- [ ] On correct PIN, call backend unlock, dismiss activity

## Deliverable
- Parent can lock child device remotely from web console
- Device shows full-screen lock with parent PIN unlock
- Backend tracks lock state and serves it via API

## Files to Create/Modify
- Create: `app/src/main/java/com/anchorguard/client/enforcement/LockCommandReceiver.kt`
- Create: `app/src/main/java/com/anchorguard/client/ui/screens/DeviceLockActivity.kt`
- Modify: `AndroidManifest.xml` — add receiver and activity declarations
- Modify: Backend `DeviceController.kt` — add lock/unlock endpoints
- Modify: Backend `DeviceService.kt` — add lock/unlock logic
- Modify: Web console Parent Dashboard — add lock button

## Test Scenario
1. Parent logs into web console → selects child device → clicks "Lock Device"
2. Tablet screen immediately shows full-screen lock
3. Parent clicks "Unlock Device" in web console
4. Tablet lock screen dismisses (or after entering PIN on device)
