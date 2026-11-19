# Module A7: SOS / Emergency Alert

## Goal
Allow the child to send an emergency alert to the parent with current location.

## Context
- No SOS functionality exists in current codebase
- `POST_NOTIFICATIONS` permission declared in AndroidManifest
- Location reporting can be leveraged (Module A6)
- Push notifications not yet implemented (Firebase FCM deferred)

## Tasks

### A7.1: Android - SOS Button
- [ ] Create `SOSScreen.kt` in `ui/screens/` — big red "SOS" button
- [ ] Add SOS button to ChildHome screen or ParentGate
- [ ] On SOS press:
  - Get current location (last known or new request)
  - Send SOS event to backend: `POST /v1/devices/{id}/sos`
  - Show "Alert sent" confirmation
  - Play alarm sound (optional)
  - Vibrate device

### A7.2: Backend - SOS Endpoint
- [ ] Add `POST /v1/devices/{id}/sos` to DeviceController
- [ ] Store SOS event: device_id, timestamp, latitude, longitude
- [ ] Return success response

### A7.3: Web Console - SOS Alerts
- [ ] Create `SOSAlertsPage.tsx` in web console
- [ ] Show list of SOS events: device, timestamp, location on map
- [ ] Mark SOS as "acknowledged" by parent
- [ ] Real-time notification (polling or WebSocket)

### A7.4: Parent Notification
- [ ] When SOS received, parent console shows alert badge
- [ ] If using web console, show browser notification (if permission granted)
- [ ] Parent can acknowledge SOS to clear alert

## Deliverable
- Child can send SOS with location from child device
- Parent receives SOS alert on web console
- SOS event shows location on map

## Files to Create/Modify
- Create: `app/src/main/java/com/anchorguard/client/ui/screens/SOSScreen.kt`
- Create: `app/src/main/java/com/anchorguard/client/data/sos/` (entity, repository)
- Modify: Backend `DeviceController.kt` — add SOS endpoint
- Modify: Backend `DeviceService.kt` — add SOS logic
- Create: Web console `SOSAlertsPage.tsx`

## Test Scenario
1. Child on tablet presses SOS button
2. Tablet sends location + SOS event to backend
3. Parent opens web console → SOS Alerts → sees tablet's location on map with timestamp
4. Parent acknowledges SOS → alert cleared
