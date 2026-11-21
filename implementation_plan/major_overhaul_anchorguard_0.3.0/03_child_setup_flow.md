# Part 3: Android App - Child Setup Flow

## Version: 0.3.0
## Duration: Week 3-4

---

## 3.1 WizardChildSignInScreen.kt (RENAME from WizardChildRegisterScreen)

### Current State
- `WizardChildRegisterScreen.kt` auto-registers device
- No sign-in UI

### Target State
- **Sign-In ONLY** (NO Create Account)
- Rename file to `WizardChildSignInScreen.kt`
- Route: `WizardChildSignIn`

### UI Layout
```
ScreenScaffold(title="Sign In to Child Device", subtitle="Enter your parent account credentials")
- OutlinedTextField: Email
- OutlinedTextField: Password (masked)
- TextButton: "Forgot Password?" → Web reset flow
- AnchorPrimaryButton: "Sign In" (enabled when valid)
- NO "Create account" option
- AnchorSecondaryButton: "Back" → WizardEntryScreen
```

### On Sign In Success
```kotlin
// Validate parent credentials via AuthRepository.login()
// On success: Navigate to WizardDeviceAuthorizationScreen
// Store: deviceRole = "child_device" in SecureSessionStore
```

### Modified: `AuthRepository.kt` - Ensure login works for child device
```kotlin
// Existing login() method works - just need to ensure it's called
// After login, child device needs to request authorization
```

---

## 3.2 WizardDeviceAuthorizationScreen.kt (NEW FILE)

### Screen: Waiting for Parent Approval
**Route:** `WizardDeviceAuthorization`

### Screen Elements
- **Title:** "Waiting for Parent Approval"
- **Body:** "Please login to your Web Console and authorize this device to continue"
- **Device Info Card:** Device name, model (auto-detected from Build.MODEL/MANUFACTURER)
- **Countdown Timer:** 180 seconds (3 minutes) - prominent display
- **Cancel Button:** Returns to WizardChildSignInScreen
- **Status Polling:** Check authorization status every 5 seconds

### UI Layout
```
ScreenScaffold(title="Device Authorization", subtitle="Waiting for parent approval...")
- Card: Device Info (name, model, "Pending Authorization" status pill)
- CountdownTimer: 3:00 → 0:00 (MM:SS format, red when < 30s)
- Progress indicator (circular or linear)
- Text: "Waiting for parent to approve on Web Console..."
- AnchorSecondaryButton: "Cancel" → WizardChildSignInScreen (stops polling)
```

### Logic
```kotlin
@Composable
fun WizardDeviceAuthorizationScreen(
    onAuthorized: () -> Unit,      // → WizardPermissionGuideScreen
    onCancelled: () -> Unit,       // → WizardChildSignInScreen
    onTimeout: () -> Unit          // Show timeout error, allow retry
) {
    val deviceId = authRepo.deviceId() ?: return
    val scope = rememberCoroutineScope()
    var secondsRemaining = remember { mutableStateOf(180) }
    var authStatus = remember { mutableStateOf<String>("pending") }
    var error = remember { mutableStateOf<String?>(null) }
    
    // 1. Request authorization on screen entry
    LaunchedEffect(Unit) {
        requestAuthorization(deviceId)
        startCountdown()
        startPolling()
    }
    
    // 2. Countdown timer (180s)
    fun startCountdown() {
        scope.launch {
            while (secondsRemaining.value > 0 && authStatus.value == "pending") {
                delay(1000)
                secondsRemaining.value--
            }
            if (secondsRemaining.value == 0 && authStatus.value == "pending") {
                authStatus.value = "timeout"
                error.value = "Authorization timed out. Please try again."
            }
        }
    }
    
    // 3. Poll authorization status every 5s
    fun startPolling() {
        scope.launch {
            while (authStatus.value == "pending" && secondsRemaining.value > 0) {
                delay(5000)
                val status = checkAuthorizationStatus(deviceId)
                when (status) {
                    "approved" -> { authStatus.value = "approved"; onAuthorized() }
                    "rejected" -> { authStatus.value = "rejected"; error.value = "Device authorization was rejected." }
                    "timeout" -> { authStatus.value = "timeout"; error.value = "Authorization timed out." }
                }
            }
        }
    }
}
```

---

## 3.3 Web Console - Device Approval UI

### Modified: `DevicesPage.tsx`

### New Section: Pending Authorizations
```tsx
// Below main device list
{pendingDevices.length > 0 && (
  <section className="card">
    <h2>Pending Authorizations</h2>
    <div className="grid-2">
      {pendingDevices.map(device => (
        <PendingDeviceCard 
          key={device.id} 
          device={device} 
          onApprove={handleApprove}
          onReject={handleReject}
        />
      ))}
    </div>
  </section>
)}
```

### New Component: `PendingDeviceCard.tsx`
```tsx
interface PendingDevice {
  id: string
  name: string
  model: string
  requestedAt: string
}

interface PendingDeviceCardProps {
  device: PendingDevice
  onApprove: (id: string) => void
  onReject: (id: string) => void
}

export function PendingDeviceCard({ device, onApprove, onReject }: PendingDeviceCardProps) {
  return (
    <div className="card pending-device">
      <div className="device-info">
        <h3>{device.name || 'Unknown Device'}</h3>
        <p className="muted">{device.model}</p>
        <p className="muted small">Requested: {new Date(device.requestedAt).toLocaleString()}</p>
        <span className="pill pill-warn">Pending</span>
      </div>
      <div className="actions">
        <button className="btn btn-primary" onClick={() => onApprove(device.id)}>Approve</button>
        <button className="btn btn-danger" onClick={() => onReject(device.id)}>Reject</button>
      </div>
    </div>
  )
}
```

### DeviceContext.tsx - Add pending devices query
```tsx
// Add to DeviceProvider
const pendingQuery = useQuery({
  queryKey: ['devices', 'pending'],
  queryFn: devicesApi.listPending, // NEW endpoint
  enabled: !!user
})
```

---

## 3.4 Backend - Device Authorization Endpoints

### New Endpoints (see Part 6 for full details)

```typescript
// POST /devices/{id}/request-authorization
// Called by child app on WizardDeviceAuthorizationScreen entry
// Body: { deviceId, deviceName, deviceModel }
// Response: { authorizationId, expiresAt }

// GET /devices/{id}/authorization-status
// Polled by child app every 5s
// Response: { status: 'pending' | 'approved' | 'rejected' | 'timeout' }

// POST /devices/{id}/approve
// Called by parent on Web Console
// Response: { success: true, deviceId }

// POST /devices/{id}/reject
// Called by parent on Web Console
// Response: { success: true }
```

---

## 3.5 WizardPermissionGuideScreen - Child Mode (Modified)

### Changes to `WizardPermissionGuideScreen.kt`

```kotlin
// When setupMode == "child_device" or "shared":
// 1. REMOVE skip option entirely (lines 247-284)
// 2. ADD ComplianceNoticeCard (from Part 2)
// 3. ADD LocationPermissionCard (NEW - mandatory)
// 4. All 5 permissions mandatory: Device Admin, Accessibility, Overlay, Battery Opt, Location
// 5. "I've granted all permissions" button enabled ONLY when ALL 5 granted
```

### New: LocationPermissionCard
```kotlin
@Composable
fun LocationPermissionCard(
    granted: Boolean,
    onGrant: () -> Unit,
    checker: EnforcementPermissionChecker
) {
    PermissionCard(
        title = "Location Access",
        description = "Required for geofence alerts, location tracking, and safety features.",
        granted = granted,
        iconContent = { if (granted) CheckIcon() else WarningIcon() },
        onGrant = { checker.requestLocationPermission(); refreshKey++ }
    )
}
```

### Modified: `EnforcementPermissionChecker.kt`
```kotlin
// ADD:
fun isLocationPermissionGranted(): Boolean {
    return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) 
        == PackageManager.PERMISSION_GRANTED
}

fun requestLocationPermission() {
    // Launch permission request via ActivityResultContracts
}
```

---

## 3.6 WizardChildGeneralSetupScreen.kt (NEW FILE)

### Screen: General Setup (Post-Permissions)
**Route:** `WizardChildGeneralSetup`

### Sections (All in Single Scrollable Screen)

#### 1. PIN Setup (MANDATORY, Not Skippable)
```
- OutlinedTextField: PIN (4-6 digits, masked)
- OutlinedTextField: Confirm PIN
- Error: mismatch, not 4-6 digits
- Submit validates and saves via PinSecurityManager
```

#### 2. Apps Allowed
```
- AnchorSwitch: "Enable App Allow List"
- When enabled: Button "Manage Allowed Apps" → AppListScreen
- Category chips: Education, Creativity, Games, Social, Utilities
- Searchable app picker with package names
```

#### 3. Games Allowed
```
- AnchorSwitch: "Enable Game Restrictions"
- Age Rating: RadioGroup (Everyone, Everyone 10+, Teen)
- Individual game toggles (fetched from installed apps with game category)
```

#### 4. Security Settings
```
- AnchorSwitch: SOS Emergency
- AnchorSwitch: Safe Zones (Geofence)
- AnchorSwitch: Remote Lock
- Each with description
```

#### 5. Location Settings
```
- AnchorSwitch: Location Tracking
- AnchorSwitch: Geofence Alerts
- Button "Manage Geofences" → Web Console link
```

#### 6. Device Settings
```
- OutlinedTextField: Device Name
- Slider: Screen Timeout (1-30 min)
- Slider: Max Volume (0-100%)
```

### Submit Button (Bottom, Fixed)
```
AnchorPrimaryButton: "Complete Setup" 
- Enabled when: PIN valid + at least one app allowed
- On click: Save all settings → Navigate to ChildHomeScreen
```

### Data Persistence
```kotlin
// Save to DevicePolicyRepository / SecureSessionStore
data class ChildSetupConfig(
    val pin: String,
    val allowedApps: List<String>, // package names
    val allowedGames: List<String>,
    val ageRating: String,
    val sosEnabled: Boolean,
    val geofenceEnabled: Boolean,
    val remoteLockEnabled: Boolean,
    val locationTrackingEnabled: Boolean,
    val geofenceAlertsEnabled: Boolean,
    val deviceName: String,
    val screenTimeoutMinutes: Int,
    val maxVolume: Int
)
```

---

## 3.7 Navigation Flow Updates

### Modified: `nav/Routes.kt`
```kotlin
object Routes {
    // ... existing ...
    const val WizardChildSignIn = "wizard_child_sign_in"
    const val WizardDeviceAuthorization = "wizard_device_authorization"
    const val WizardChildGeneralSetup = "wizard_child_general_setup"
}
```

### Modified: `AnchorGuardNavHost.kt`
```kotlin
composable(Routes.WizardChildSignIn) { WizardChildSignInScreen(...) }
composable(Routes.WizardDeviceAuthorization) { WizardDeviceAuthorizationScreen(...) }
composable(Routes.WizardChildGeneralSetup) { WizardChildGeneralSetupScreen(...) }
```

---

## 3.8 Files to Create/Modify

### New Files
```
ui/screens/wizard/WizardChildSignInScreen.kt (RENAME from WizardChildRegisterScreen)
ui/screens/wizard/WizardDeviceAuthorizationScreen.kt
ui/screens/wizard/WizardChildGeneralSetupScreen.kt
ui/components/PendingDeviceCard.tsx (Web Console)
```

### Modified Files
```
ui/screens/wizard/WizardPermissionGuideScreen.kt
auth/data/AuthRepository.kt (if new methods needed)
enforcement/EnforcementPermissionChecker.kt (add location)
nav/Routes.kt
nav/AnchorGuardNavHost.kt
// Web Console:
web-console/src/pages/DevicesPage.tsx
web-console/src/context/DeviceContext.tsx
web-console/src/api/endpoints.ts
web-console/src/components/devices/PendingDeviceCard.tsx
```

---

## 3.9 Codegraph Checkpoints

### At Start
- `codegraph_explore` query: "WizardChildRegisterScreen WizardPermissionGuideScreen EnforcementPermissionChecker"
- Review current child flow and permission checker

### At End
- `codegraph_explore` query: "WizardDeviceAuthorizationScreen WizardChildGeneralSetupScreen PendingDeviceCard"
- Verify: Child sign in → request auth → parent approve → permissions (no skip) → general setup → child home

---

## 3.10 Testing

### Unit Tests
- Countdown timer logic (180s, format MM:SS)
- Authorization polling logic (5s interval, state transitions)
- PIN validation in general setup
- ChildSetupConfig serialization

### Integration Tests
- Full child flow: Sign in → request auth → parent approve (Web Console) → permissions → general setup → child home
- Timeout handling: 180s expiry → error screen → retry
- Rejection handling: Parent rejects → error screen → back to sign in
- All 5 permissions mandatory, location included

### Device Testing
- Timer visible and accurate
- Cancel button works during countdown
- Polling doesn't drain battery
- Web Console approve/reject reflects immediately (WebSocket/polling)
- General setup saves all config correctly

---

## 3.11 Dependencies

- Part 1 (themes) - for consistent UI
- Part 2 (AuthRepository.login) - for child sign in
- Part 6 (Backend authorization endpoints, WebSocket/polling)
- Web Console Part 5 (Pending device UI)