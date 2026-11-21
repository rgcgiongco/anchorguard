# Part 2: Android App - Parent Setup Flow

## Version: 0.3.0
## Duration: Week 2-3

---

## 2.1 WizardParentAuthScreen Revamp (`WizardParentAuthScreen.kt`)

### Current State
- Single screen with Email, Password, Name (optional)
- Both "Sign in" and "Create account" buttons
- Back button exists but may not work correctly

### Target State
- **Sign In Flow**: Email + Password only + "Forgot Password" link
- **Create Account Flow**: Navigate to NEW `WizardParentRegisterScreen`
- Back button works correctly (navigate to WizardEntryScreen)

### Changes to `WizardParentAuthScreen.kt`
```kotlin
// REMOVE: Name field (lines 38, 63-68)
// REMOVE: "Create account" button (lines 91-106)
// ADD: "Forgot Password?" TextButton
// MODIFY: "Create account" text → navigates to WizardParentRegisterScreen
// FIX: Back button → onBack() properly
```

---

## 2.2 WizardParentRegisterScreen.kt (NEW FILE)

### Screen: Parent Account Registration
**Route:** `WizardParentRegister`

### Fields (All Required)
1. **Full Name** - Text, required
2. **Email** - Email input, required
3. **Contact Number** - Phone input with country code picker, required
4. **Password** - Min 8 chars, required
5. **Confirm Password** - Must match, required

### UI Layout
```
ScreenScaffold(title="Create Parent Account", subtitle="Enter your details to create an account")
- OutlinedTextField: Full Name
- OutlinedTextField: Email
- PhoneInputField: Contact Number (with country code)
- OutlinedTextField: Password (masked)
- OutlinedTextField: Confirm Password (masked)
- Error display
- AnchorPrimaryButton: "Create Account" (enabled when valid)
- TextButton: "Already have an account? Sign in" → WizardParentAuthScreen
- AnchorSecondaryButton: "Back" → WizardEntryScreen
```

### On Submit
```kotlin
// Call AuthRepository.registerWithPhone(name, email, phone, password)
// On Success: Show notification "Check your email for activation link"
// Navigate to WizardParentAuthScreen (or show success screen)
// On Failure: Show error message
```

---

## 2.3 AuthRepository.kt - New Method

```kotlin
suspend fun registerWithPhone(
    name: String,
    email: String,
    phone: String,
    password: String
): NetworkResult<AuthTokensResponse> {
    val result = safeApiCall(appContext) {
        api().registerWithPhone(RegisterWithPhoneRequest(
            name = name.trim(),
            email = email.trim().lowercase(Locale.ROOT),
            phone = phone.trim(),
            password = password
        ))
    }
    if (result is NetworkResult.Success) {
        val body = result.data
        session.setParentTokens(body.accessToken, body.refreshToken)
        session.setParentEmail(email.trim().lowercase(Locale.ROOT))
        session.setUserTier(body.tier)
    }
    return result
}
```

### New API Request/Response Models
```kotlin
data class RegisterWithPhoneRequest(
    val name: String,
    val email: String,
    val phone: String,
    val password: String
)

// Response: AuthTokensResponse (existing) + tempPassword field
data class AuthTokensResponse(
    val accessToken: String,
    val refreshToken: String,
    val tier: String,
    val tempPassword: String? // NEW: 8-char random from backend
)
```

---

## 2.4 WizardParentPinScreen.kt (NEW FILE)

### Screen: Parent PIN Setup (Mandatory)
**Route:** `WizardParentPin`
**Flow:** After successful sign in → PIN Setup → Parent Dashboard

### Requirements
- 4-6 digit PIN (mandatory, NOT skippable)
- Confirm PIN
- Submit button → navigates to Parent Dashboard
- Store PIN via `PinSecurityManager`

### UI Layout
```
ScreenScaffold(title="Create Parent PIN", subtitle="Set a 4-6 digit PIN to protect parent controls. This is required.")
- OutlinedTextField: PIN (masked, digits only, max 6)
- OutlinedTextField: Confirm PIN (masked, digits only, max 6)
- Error display (PINs don't match, not 4-6 digits)
- AnchorPrimaryButton: "Save PIN & Continue" (enabled when valid)
- NO skip option
- NO back button (PIN is mandatory after sign in)
```

### On Submit
```kotlin
if (pin != confirm) { error = "PINs do not match." }
else if (pin.length < 4 || pin.length > 6) { error = "PIN must be 4-6 digits." }
else {
    val ok = pinManager.createPin(pin)
    if (ok) {
        // Mark parent setup complete
        store.setParentSetupCompleted(true)
        onContinue() // → ParentDashboardScreen
    } else {
        error = "Failed to save PIN. Try again."
    }
}
```

---

## 2.5 WizardPermissionGuideScreen - Disable for Parent Setup

### Modified: `WizardPermissionGuideScreen.kt`

```kotlin
@Composable
fun WizardPermissionGuideScreen(
    onAllGranted: () -> Unit,
    onSkipped: () -> Unit, // REMOVE for parent mode
    onBack: () -> Unit,
    currentStep: Int = 3,
    totalSteps: Int = 6,
    setupMode: String = "child_device" // NEW PARAMETER
) {
    // AT TOP OF COMPOSABLE:
    if (setupMode == "parent") {
        // Skip this screen entirely for parent setup
        LaunchedEffect(Unit) { onAllGranted() }
        return
    }
    
    // FOR CHILD/SHARED: Remove skip option entirely
    // REMOVE lines 247-284: Skip permissions button + dialog
    
    // ADD: Google Play Compliance Notice Card
    ComplianceNoticeCard()
    
    // ADD: Location Permission Card (new)
    LocationPermissionCard()
}
```

### New: ComplianceNoticeCard
```kotlin
@Composable
fun ComplianceNoticeCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Info, contentDescription = "Info", tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Google Play Store Compliance",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Text(
                text = "AnchorGuard requires Device Admin, Accessibility, Overlay, Battery Optimization, and Location permissions to enforce app blocking, screen time limits, and remote device lock as described on Google Play Store. These permissions are mandatory for child safety features and cannot be skipped.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}
```

### New: LocationPermissionCard
```kotlin
@Composable
fun LocationPermissionCard(
    granted: Boolean,
    onGrant: () -> Unit
) {
    // Similar to existing PermissionCard but for Location
    // Uses EnforcementPermissionChecker.requestLocation()
}
```

---

## 2.6 ParentDashboardScreen.kt - Major Rewrite

### Current State
- Basic cards with placeholder text
- No real device data
- Not mobile/tablet optimized

### Target State: Mobile/Tablet Optimized Dashboard (Matches Web Console)

### New Features
1. **Stats Grid** (responsive: 2 cols phone, 4 cols tablet)
   - Linked devices count
   - Online now count
   - Paired count
   - Subscription tier badge

2. **Device List Section** - Real-time device cards
   - Device name, model, online/offline status
   - Last seen timestamp
   - Quick actions: App Policy, Schedule, Location, Remove

3. **Selected Device Detail** - Expandable panel
   - Device info, current status, quick actions

4. **Quick Actions Row**
   - Device List, Usage Reports, Security, Account

5. **Pull-to-refresh** for live data

### New Component: `DeviceCard.kt`
```kotlin
@Composable
fun DeviceCard(
    device: Device, // New data class
    onAppPolicyClick: () -> Unit,
    onScheduleClick: () -> Unit,
    onLocationClick: () -> Unit,
    onRemoveClick: () -> Unit
) {
    // Card with device info, status pill, action buttons
}
```

### New: `DashboardViewModel.kt`
```kotlin
@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val authRepo: AuthRepository,
    private val deviceRepo: DeviceRepository
) : ViewModel() {
    
    val devices = mutableStateOf<List<Device>>(emptyList())
    val selectedDevice = mutableStateOf<Device?>(null)
    val isLoading = mutableStateOf(false)
    val subscriptionTier = mutableStateOf<String>("free")
    
    fun loadDevices() { /* fetch from API */ }
    fun refreshDevices() { /* pull-to-refresh */ }
    fun selectDevice(device: Device) { selectedDevice.value = device }
}
```

### Data Classes
```kotlin
data class Device(
    val id: String,
    val name: String,
    val model: String,
    val isOnline: Boolean,
    val lastSeen: String?,
    val isPaired: Boolean,
    val hexId: String
)
```

---

## 2.7 Navigation Flow Updates

### Modified: `nav/Routes.kt`
```kotlin
object Routes {
    // ... existing ...
    const val WizardParentRegister = "wizard_parent_register"
    const val WizardParentPin = "wizard_parent_pin"
}
```

### Modified: `AnchorGuardNavHost.kt`
```kotlin
// Add routes:
composable(Routes.WizardParentRegister) { WizardParentRegisterScreen(...) }
composable(Routes.WizardParentPin) { WizardParentPinScreen(...) }
```

---

## 2.8 Files to Create/Modify

### New Files
```
ui/screens/wizard/WizardParentRegisterScreen.kt
ui/screens/wizard/WizardParentPinScreen.kt
ui/components/DeviceCard.kt
ui/viewmodel/DashboardViewModel.kt
ui/model/Device.kt
```

### Modified Files
```
ui/screens/wizard/WizardParentAuthScreen.kt
ui/screens/wizard/WizardPermissionGuideScreen.kt
ui/screens/ParentDashboardScreen.kt (MAJOR REWRITE)
auth/data/AuthRepository.kt
nav/Routes.kt
nav/AnchorGuardNavHost.kt
```

---

## 2.9 Codegraph Checkpoints

### At Start
- `codegraph_explore` query: "WizardParentAuthScreen AuthRepository ParentDashboardScreen"
- Review all parent flow screens and auth repository

### At End
- `codegraph_explore` query: "WizardParentRegisterScreen WizardParentPinScreen DashboardViewModel"
- Verify: Parent registration → email activation → sign in → PIN → dashboard works
- Check: Permission screen skipped for parent, compliance notice shown for child

---

## 2.10 Testing

### Unit Tests
- `registerWithPhone` API call with valid/invalid data
- PIN validation (4-6 digits, match)
- DashboardViewModel device loading/refresh
- Permission guide screen conditional rendering

### Integration Tests
- Full parent flow: Register → activation email → sign in → PIN → dashboard
- Back navigation works at each step
- Forgot password flow
- Dashboard shows live device data, pull-to-refresh works

### Device Testing
- Phone: stats grid 2 cols, device cards stacked
- Tablet: stats grid 4 cols, device cards in grid
- Orientation changes preserve state
- Large device lists scroll properly

---

## 2.11 Dependencies

- Part 1 (themes, language selector) - for consistent theming
- Backend Part 6 (registerWithPhone API, activation email)