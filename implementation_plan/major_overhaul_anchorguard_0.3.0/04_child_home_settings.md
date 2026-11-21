# Part 4: Android App - Child Home Screen & Settings

## Version: 0.3.0
## Duration: Week 5-6

---

## 4.1 ChildHomeScreen.kt - Major Rewrite (Kids Place Style)

### Reference Design
- `childhome.jpg` - Main child home launcher grid
- `../../../../Desktop/childhome1.jpg` - Settings dialog (apps allowed, games allowed, security, location, device)
- `../../../../Desktop/childhome2.jpg` - Additional settings screens

### Current State
- Basic info cards: Protection status, Allowed apps placeholder, Time remaining
- Parent access button, SOS button
- No launcher-style app grid
- No exit/settings in top bar

### Target State: Full-Screen Launcher (Kiosk Mode)

### New Layout Structure

```
┌─────────────────────────────────────────────────────────────┐
│  [Child Avatar/Name]                    [Settings ⚙] [Exit ✕] │  ← Top Bar (PIN required)
├─────────────────────────────────────────────────────────────┤
│                                                             │
│   ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐          │
│   │  App 1  │ │  App 2  │ │  App 3  │ │  App 4  │          │  ← Allowed Apps Grid
│   │  📱     │ │  🎮     │ │  📚     │ │  🎨     │          │     (4 cols phone,
│   │ Name    │ │ Name    │ │ Name    │ │ Name    │          │      6 cols tablet)
│   │ 45m     │ │ 1h 20m  │ │ 30m     │ │ 1h      │          │
│   └─────────┘ └─────────┘ └─────────┘ └─────────┘          │
│                                                             │
│   ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐          │
│   │  App 5  │ │  App 6  │ │  App 7  │ │  App 8  │          │
│   └─────────┘ └─────────┘ └─────────┘ └─────────┘          │
│                                                             │
├─────────────────────────────────────────────────────────────┤
│  [SOS Emergency]              Time Remaining: 2h 15m       │  ← Bottom Bar
└─────────────────────────────────────────────────────────────┘
```

### Top Bar Components

**ChildAvatar.kt**
```kotlin
@Composable
fun ChildAvatar(
    name: String,
    onClick: () -> Unit // Requires PIN → ChildSettingsScreen
) {
    // Circular avatar with first letter or icon
    // Click → show PIN dialog → ChildSettingsScreen
}
```

**TopBarActions.kt**
```kotlin
@Composable
fun TopBarActions(
    onSettingsClick: () -> Unit, // Requires PIN
    onExitClick: () -> Unit      // Requires PIN → Exit to parent mode
) {
    Row(horizontalArrangement = Arrangement.End) {
        IconButton(onClick = { verifyPinThen(onSettingsClick) }) {
            Icon(Icons.Filled.Settings, contentDescription = "Settings")
        }
        IconButton(onClick = { verifyPinThen(onExitClick) }) {
            Icon(Icons.Filled.Close, contentDescription = "Exit Child Mode")
        }
    }
}
```

### Main Grid: Allowed Apps

**AllowedAppsGrid.kt**
```kotlin
@Composable
fun AllowedAppsGrid(
    apps: List<AllowedApp>,
    onAppClick: (String) -> Unit, // packageName → launch
    timeRemainingMap: Map<String, Long> // packageName → milliseconds
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(if (isTablet) 6 else 4),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(apps) { app ->
            AppGridCard(
                app = app,
                timeRemaining = timeRemainingMap[app.packageName] ?: 0L,
                onClick = { onAppClick(app.packageName) }
            )
        }
    }
}

@Composable
fun AppGridCard(app: AllowedApp, timeRemaining: Long, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // App icon (load via PackageManager)
            AppIcon(app.packageName, size = 48.dp)
            Spacer(Modifier.height(8.dp))
            Text(app.name, style = MaterialTheme.typography.labelLarge, maxLines = 1, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center)
            Spacer(Modifier.height(4.dp))
            Text(formatTimeRemaining(timeRemaining), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
```

### Bottom Bar

**BottomBar.kt**
```kotlin
@Composable
fun BottomBar(
    totalTimeRemaining: Long,
    onSosClick: () -> Unit,
    onParentModeClick: () -> Unit // Requires PIN
) {
    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        // SOS Button (full width, prominent)
        Button(
            onClick = onSosClick,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                Icon(Icons.Filled.Warning, contentDescription = "SOS", tint = MaterialTheme.colorScheme.onError)
                Spacer(Modifier.width(8.dp))
                Text("SOS — Emergency Alert", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onError)
            }
        }
        Spacer(Modifier.height(12.dp))
        // Time remaining + Parent mode
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Time Remaining: ${formatTimeRemaining(totalTimeRemaining)}", style = MaterialTheme.typography.bodyMedium)
            TextButton(onClick = { verifyPinThen(onParentModeClick) }) {
                Text("Parent Mode", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}
```

### Full ChildHomeScreen.kt

```kotlin
@Composable
fun ChildHomeScreen(
    onOpenParentGate: () -> Unit,
    onOpenSos: () -> Unit,
    onOpenSettings: () -> Unit, // NEW: requires PIN
    onExitChildMode: () -> Unit  // NEW: requires PIN
) {
    val context = LocalContext.current
    val state by EnforcementController.state.collectAsState()
    val allowedApps = getAllowedApps() // From DevicePolicyRepository
    val timeRemainingMap = getTimeRemainingMap() // From UsageTracker
    
    ScreenScaffold(
        title = "", // No title in launcher mode
        subtitle = "",
        topBar = {
            ChildTopBar(
                childName = getChildName(),
                onSettingsClick = { verifyPinThen(onOpenSettings) },
                onExitClick = { verifyPinThen(onExitChildMode) }
            )
        }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(0.dp)) {
                // Main content - scrollable app grid
                VerticalScroll(Modifier.fillMaxSize()) {
                    AllowedAppsGrid(
                        apps = allowedApps,
                        onAppClick = { pkg -> launchApp(context, pkg) },
                        timeRemainingMap = timeRemainingMap
                    )
                }
                // Bottom bar - fixed
                BottomBar(
                    totalTimeRemaining = getTotalTimeRemaining(),
                    onSosClick = onOpenSos,
                    onParentModeClick = onOpenParentGate
                )
            }
        }
    }
}
```

---

## 4.2 ChildSettingsScreen.kt (NEW FILE)

### Access Flow
1. User taps Settings ⚙ in Child Home top bar
2. **PIN Verification Dialog** → Enter parent PIN
3. On success → ChildSettingsScreen
4. On fail → Error, max 3 attempts then lockout

### PIN Verification Dialog
```kotlin
@Composable
fun PinVerificationDialog(
    onVerified: () -> Unit,
    onCancel: () -> Unit
) {
    // Standard PIN entry with 4-6 digits
    // Uses PinSecurityManager.verifyPin()
    // On success: onVerified()
    // On cancel: onCancel()
}
```

### Settings Screen Structure (Tabbed or Sectioned)

**ChildSettingsScreen.kt**
```kotlin
@Composable
fun ChildSettingsScreen(onBack: () -> Unit) {
    val pagerState = rememberPagerState()
    val scope = rememberCoroutineScope()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Child Settings") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, "") } }
            )
        }
    ) { padding ->
        HorizontalPager(state = pagerState, modifier = Modifier.padding(padding)) {
            // Page 1: Apps & Games
            AppsGamesSettingsPage()
            // Page 2: Security
            SecuritySettingsPage()
            // Page 3: Location
            LocationSettingsPage()
            // Page 4: Device
            DeviceSettingsPage()
            // Page 5: PIN Management
            PinManagementPage()
        }
        // Page indicator dots
    }
}
```

---

### Page 1: Apps & Games Settings (`AppsGamesSettingsPage.kt`)

```kotlin
@Composable
fun AppsGamesSettingsPage() {
    Column(modifier = Modifier.padding(16.dp).fillMaxSize(), verticalArrangement = Arrangement.spacedBy(24.dp)) {
        // Apps Allowed Section
        SettingsSection(title = "Apps Allowed") {
            AnchorSwitch(
                checked = appAllowListEnabled,
                onCheckedChange = { setAppAllowListEnabled(it) },
                label = "Enable App Allow List",
                description = "Only allowed apps can be launched in Child Mode"
            )
            if (appAllowListEnabled) {
                Button(onClick = { navigateToAppPicker() }) {
                    Text("Manage Allowed Apps")
                }
                // Show current count: "12 apps allowed"
            }
        }
        
        // Games Allowed Section
        SettingsSection(title = "Games Allowed") {
            AnchorSwitch(
                checked = gameRestrictionsEnabled,
                onCheckedChange = { setGameRestrictionsEnabled(it) },
                label = "Enable Game Restrictions",
                description = "Filter games by age rating"
            )
            if (gameRestrictionsEnabled) {
                // Age Rating Radio Group
                RadioGroup(
                    options = listOf("Everyone", "Everyone 10+", "Teen"),
                    selected = selectedAgeRating,
                    onSelect = { setSelectedAgeRating(it) }
                )
                Button(onClick = { navigateToGamePicker() }) {
                    Text("Manage Individual Games")
                }
            }
        }
    }
}
```

---

### Page 2: Security Settings (`SecuritySettingsPage.kt`)

```kotlin
@Composable
fun SecuritySettingsPage() {
    Column(modifier = Modifier.padding(16.dp).fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        SettingsSection(title = "Security Features") {
            SettingRow(
                title = "SOS Emergency",
                description = "Press power button 3x or use SOS button to alert parent",
                control = { AnchorSwitch(checked = sosEnabled, onCheckedChange = { setSosEnabled(it) }) }
            )
            SettingRow(
                title = "Safe Zones (Geofence)",
                description = "Get alerts when device enters/leaves designated areas",
                control = { AnchorSwitch(checked = geofenceEnabled, onCheckedChange = { setGeofenceEnabled(it) }) }
            )
            SettingRow(
                title = "Remote Lock",
                description = "Allow parent to remotely lock this device",
                control = { AnchorSwitch(checked = remoteLockEnabled, onCheckedChange = { setRemoteLockEnabled(it) }) }
            )
        }
        
        // SOS Configuration (if enabled)
        if (sosEnabled) {
            SettingsSection(title = "SOS Configuration") {
                SettingRow(
                    title = "Emergency Contacts",
                    description = "Manage who receives SOS alerts",
                    control = { Button(onClick = { navigateToEmergencyContacts() }) { Text("Manage") } }
                )
                SettingRow(
                    title = "SOS Message Template",
                    description = "Customize the emergency message",
                    control = { Button(onClick = { navigateToSosTemplate() }) { Text("Edit") } }
                )
            }
        }
    }
}
```

---

### Page 3: Location Settings (`LocationSettingsPage.kt`)

```kotlin
@Composable
fun LocationSettingsPage() {
    Column(modifier = Modifier.padding(16.dp).fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        SettingsSection(title = "Location Services") {
            SettingRow(
                title = "Location Tracking",
                description = "Share device location with parent console",
                control = { AnchorSwitch(checked = locationTrackingEnabled, onCheckedChange = { setLocationTrackingEnabled(it) }) }
            )
            SettingRow(
                title = "Geofence Alerts",
                description = "Notify when entering/leaving safe zones",
                control = { AnchorSwitch(checked = geofenceAlertsEnabled, onCheckedChange = { setGeofenceAlertsEnabled(it) }) }
            )
        }
        
        if (geofenceAlertsEnabled) {
            SettingsSection(title = "Safe Zones") {
                Text("Manage safe zones from the Parent Web Console")
                Button(onClick = { openWebConsoleGeofences() }) {
                    Text("Open Web Console")
                }
            }
        }
        
        SettingsSection(title = "Location History") {
            SettingRow(
                title = "Retention Period",
                description = "Free: 7 days | Paid: 6 months",
                control = { Text(currentRetention, style = MaterialTheme.typography.bodyMedium) }
            )
        }
    }
}
```

---

### Page 4: Device Settings (`DeviceSettingsPage.kt`)

```kotlin
@Composable
fun DeviceSettingsPage() {
    Column(modifier = Modifier.padding(16.dp).fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        SettingsSection(title = "Device Configuration") {
            SettingRow(
                title = "Device Name",
                description = "Name shown in parent dashboard",
                control = { OutlinedTextField(value = deviceName, onValueChange = { setDeviceName(it) }, label = { Text("Device Name") }) }
            )
            SettingRow(
                title = "Screen Timeout",
                description = "Auto-lock after inactivity",
                control = { 
                    Slider(value = screenTimeoutMinutes, onValueChange = { setScreenTimeoutMinutes(it) }, valueRange = 1f..30f, steps = 29)
                    Text("${screenTimeoutMinutes} min")
                }
            )
            SettingRow(
                title = "Maximum Volume",
                description = "Limit maximum media volume",
                control = { 
                    Slider(value = maxVolume, onValueChange = { setMaxVolume(it) }, valueRange = 0f..100f, steps = 20)
                    Text("${maxVolume}%")
                }
            )
        }
        
        SettingsSection(title = "Advanced") {
            SettingRow(
                title = "Kiosk Mode",
                description = "Lock to child home (prevents exiting apps)",
                control = { AnchorSwitch(checked = kioskModeEnabled, onCheckedChange = { setKioskModeEnabled(it) }) }
            )
            SettingRow(
                title = "Block Notifications",
                description = "Hide notifications in Child Mode",
                control = { AnchorSwitch(checked = blockNotificationsEnabled, onCheckedChange = { setBlockNotificationsEnabled(it) }) }
            )
        }
    }
}
```

---

### Page 5: PIN Management (`PinManagementPage.kt`)

```kotlin
@Composable
fun PinManagementPage() {
    Column(modifier = Modifier.padding(16.dp).fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        SettingsSection(title = "Parent PIN") {
            Text("Change the PIN used to access parent controls and settings", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(16.dp))
            
            // Current PIN verification
            OutlinedTextField(
                value = currentPin,
                onValueChange = { setCurrentPin(it) },
                label = { Text("Current PIN") },
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true
            )
            Spacer(Modifier.height(12.dp))
            
            // New PIN
            OutlinedTextField(
                value = newPin,
                onValueChange = { setNewPin(it) },
                label = { Text("New PIN (4-6 digits)") },
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true
            )
            Spacer(Modifier.height(12.dp))
            
            // Confirm New PIN
            OutlinedTextField(
                value = confirmPin,
                onValueChange = { setConfirmPin(it) },
                label = { Text("Confirm New PIN") },
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true
            )
            
            if (pinError != null) {
                Text(pinError!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
            
            Spacer(Modifier.height(24.dp))
            AnchorPrimaryButton(
                text = "Update PIN",
                onClick = { 
                    if (verifyCurrentPin() && newPin == confirmPin && newPin.length in 4..6) {
                        pinManager.createPin(newPin)
                        // Success feedback
                    } else {
                        pinError = "Invalid PIN. Check current PIN and ensure new PINs match (4-6 digits)."
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
```

---

## 4.3 Shared Device Flow Alignment

### Requirement
"This device is shared — Parent + Child" follows SAME flow as "This is a child's device"

### Modified: `WizardEntryScreen.kt`

```kotlin
// In onBothDevices callback:
AnchorPrimaryButton(text = "Set up as shared", onClick = {
    store.setDeviceRole("both")
    store.setSetupMode("shared")
    store.setActiveMode("parent") // Default to parent mode initially
    // NAVIGATE TO CHILD FLOW (same as onChildDevice)
    onChildDevice() // Reuse child sign-in flow
})
```

### After Setup Complete
- Child Home Screen with **Parent Mode toggle** in bottom bar
- Parent Mode requires PIN (same as Exit button)
- In Parent Mode: Full app access, can access Parent Dashboard
- In Child Mode: Restricted to allowed apps, Child Home UI

### Modified: `ChildHomeScreen.kt` - Add Parent Mode Toggle

```kotlin
// In BottomBar, add:
if (store.isBothRole()) {
    TextButton(onClick = { verifyPinThen(onSwitchToParentMode) }) {
        Text(store.activeMode() == "parent" ? "Switch to Child Mode" : "Switch to Parent Mode")
    }
}
```

---

## 4.4 Files to Create/Modify

### New Files
```
ui/screens/ChildHomeScreen.kt (MAJOR REWRITE)
ui/screens/ChildSettingsScreen.kt
ui/screens/settings/AppsGamesSettingsPage.kt
ui/screens/settings/SecuritySettingsPage.kt
ui/screens/settings/LocationSettingsPage.kt
ui/screens/settings/DeviceSettingsPage.kt
ui/screens/settings/PinManagementPage.kt
ui/components/ChildTopBar.kt
ui/components/AllowedAppsGrid.kt
ui/components/AppGridCard.kt
ui/components/BottomBar.kt
ui/components/ChildAvatar.kt
ui/components/TopBarActions.kt
ui/components/SettingsSection.kt
ui/components/SettingRow.kt
ui/components/PinVerificationDialog.kt
```

### Modified Files
```
ui/screens/wizard/WizardEntryScreen.kt (shared device flow)
nav/Routes.kt (add ChildSettings route)
nav/AnchorGuardNavHost.kt
auth/security/PinSecurityManager.kt (if needed)
enforcement/EnforcementController.kt (parent mode switching)
```

---

## 4.5 Codegraph Checkpoints

### At Start
- `codegraph_explore` query: "ChildHomeScreen PinSecurityManager EnforcementController"
- Review current child home and PIN verification

### At End
- `codegraph_explore` query: "ChildSettingsScreen AllowedAppsGrid PinVerificationDialog"
- Verify: Child Home launcher grid works, settings accessible via PIN, all 5 settings pages functional, shared device toggles modes

---

## 4.6 Testing

### Unit Tests
- `AllowedAppsGrid` layout (4 cols phone, 6 tablet)
- `PinVerificationDialog` success/failure/lockout
- Settings pages save/load config correctly
- Time formatting (MM:SS, HH:MM)
- Parent/Child mode toggle logic

### Integration Tests
- Child Home → Settings (PIN) → each page → save → back to Child Home
- App launch from grid respects time limits
- SOS button triggers alert
- Exit Child Mode (PIN) → Parent Dashboard
- Shared device: Parent ↔ Child mode switch (PIN)

### Device Testing
- Launcher grid smooth scrolling
- PIN dialog appears correctly
- Settings pages scroll properly
- Tablet layout: 6-col grid, side-by-side sections
- Kiosk mode prevents home/recents buttons
- Orientation changes preserve state

---

## 4.7 Dependencies

- Part 1 (themes) - consistent theming
- Part 2 (PinSecurityManager, Parent Dashboard) - PIN verification, parent mode
- Part 3 (ChildSetupConfig) - settings persisted from general setup
- Part 6 (Backend) - geofence management via Web Console