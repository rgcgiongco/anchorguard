# Module A4: Multi-Child Profiles (Users Screen)

## Goal
Support multiple child profiles on a single parent account, allowing different settings and limits per child.

## Context
- `UsersScreen.tsx` exists in web console — placeholder
- `DeviceUser` entity exists in Android app (`data/local/entity/DeviceUser.kt`)
- `DeviceUserDao` exists with `getByDeviceId()` queries
- Backend `DeviceService.list()` returns devices by `parentId`
- Each device can represent one child profile

## Tasks

### A4.1: Backend - Child Profile Management
- [ ] Verify `GET /v1/devices` returns all devices for parent
- [ ] Verify `PATCH /v1/devices/{id}` can update device name (child name)
- [ ] Verify `DELETE /v1/devices/{id}` removes device (unlink)
- [ ] Add `POST /v1/devices` to register a new child device under parent account

### A4.2: Android - Users Screen
- [ ] Update `UsersScreen.kt` in `ui/screens/`
- [ ] List all child devices linked to parent account
- [ ] Show each child's: name, device model, online/offline status, last seen
- [ ] Allow renaming child device (tap to edit name)
- [ ] Allow removing/unlinking a device
- [ ] Pull-to-refresh to sync with backend

### A4.3: Device Selection / Switching
- [ ] On ParentDashboard, show current device being managed
- [ ] Dropdown or selector to switch between multiple child devices
- [ ] When switching device, load that device's settings and policies

### A4.4: Web Console - Users Page
- [ ] Verify `UsersPage.tsx` in web console lists all devices for parent
- [ ] Verify rename and remove device actions work
- [ ] Verify each device card links to its own Device Settings page

## Deliverable
- Parent can see all linked child devices
- Parent can rename each child device
- Parent can unlink/remove a device
- Device-specific settings load when switching between children

## Test Credentials
| Account | Email | Password |
|---------|-------|----------|
| Admin | `anchor_admin@anchorguard.com` | `Gabby01172020!!!` |
| Parent | `parent@anchorguard.com` | `TestParent123!` |
