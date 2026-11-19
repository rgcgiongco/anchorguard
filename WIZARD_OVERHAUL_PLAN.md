# AnchorGuard Wizard Overhaul — Implementation Plan

## Executive Summary

Replace the current 2-screen entry (Welcome → Role Selection) with a **single unified entry screen** that branches into three explicit paths:
- **Path A**: Parent device (auth → permissions → pairing → safety → dashboard)
- **Path B**: Child device (register → permissions → pairing → safety → child home)
- **Path C**: Parent adding child device (register → permissions → pairing → safety → child home, no auth)

Plus UX improvements: progress indicator, guided permissions, unified pairing UI, optional safety with warnings, standardized back navigation, and finish-with-pending-items.

---

## Current vs Proposed Flow

### Current (7 screens, implicit branching)
```
Welcome → RoleSelection → (ParentAuth | ChildRegister) → Permissions → Pairing → Safety → Complete
```

### Proposed (8 screens, explicit branching)
```
EntryScreen → (ParentAuth | DeviceRegister) → Permissions → Pairing → Safety → Complete
                │                              │            │
                ▼                              ▼            ▼
           PATH A: auth                  PATH B/C:      Unified pairing
                            register (auto)   screen
```

---

## New Screens to Create

### 1. `WizardEntryScreen` (replaces Welcome + RoleSelection)
**File**: `app/src/main/java/com/anchorguard/client/ui/screens/wizard/WizardEntryScreen.kt`

Three cards with icons, descriptions, and primary buttons:
- **Card 1**: "I'm a parent — sign in or create account" → navigates to `WizardParentAuth`
- **Card 2**: "This is a child's device" → navigates to `WizardChildRegister`
- **Card 3**: "I'm a parent adding a new child device" → navigates to `WizardChildRegister` (same screen, different entry flag)

Pass a `SetupMode` enum via navigation args: `PARENT_DEVICE`, `CHILD_DEVICE`, `PARENT_ADDING_CHILD`.

---

## Screens to Modify

### 2. `WizardChildRegisterScreen` — Add mode awareness
**File**: `app/src/main/java/com/anchorguard/client/ui/screens/wizard/WizardChildRegisterScreen.kt`

Accept `setupMode` arg (`CHILD_DEVICE` or `PARENT_ADDING_CHILD`).
- **CHILD_DEVICE**: Current behavior (auto-register, show "Registering this device…")
- **PARENT_ADDING_CHILD**: Change subtitle to "Registering new child device…" — same logic, different copy.
- On success: navigate to `WizardPermissions` (not `WizardPairing` directly — permissions first).

### 3. `WizardPermissionsScreen` — Guided permissions + progress
**File**: `app/src/main/java/com/anchorguard/client/ui/screens/wizard/WizardPermissionGuideScreen.kt`

**Changes:**
- Add **progress indicator** at top: "Step 3 of 6" (or dynamic based on path)
- Each permission card gets an **expandable "How to grant"** section with step-by-step instructions
- Use `startActivityForResult`-style intents where possible so app auto-detects grant:
  - Device Admin: `DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN`
  - Overlay: `Settings.ACTION_MANAGE_OVERLAY_PERMISSION`
  - Accessibility: `Settings.ACTION_ACCESSIBILITY_SETTINGS` (no direct result, but can poll)
  - Battery: `Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS`
- Add **"Skip for now — I'll set up later"** button (enabled always, but shows warning)
- "I've granted all permissions" button only enabled when all 4 detected as granted
- Auto-refresh on `ON_RESUME` (already implemented — keep)

### 4. `WizardPairingScreen` — Unified UI
**File**: `app/src/main/java/com/anchorguard/client/ui/screens/wizard/WizardPairingScreen.kt`

**Changes:**
- **Always show the pairing code prominently** (large, copyable, with expiry)
- **Always show an input field** below it labeled "Enter child's pairing code"
- Two primary actions:
  - "I've entered the code on the other device" → verifies pairing via backend
  - "Skip for now" → proceeds to safety
- Remove the conditional UI split (parent session vs no session)
- Add "Copy code" button with toast confirmation

### 5. `WizardSafetyOptionsScreen` — Reordered, optional with warnings
**File**: `app/src/main/java/com/anchorguard/client/ui/screens/wizard/WizardSafetyOptionsScreen.kt`

**Changes:**
- **Reorder**: PIN → SOS → Geofence (PIN first, most critical)
- **PIN**: Required for parent path, optional for child paths. Show "Set up PIN later" link instead of burying in "Skip all".
- **Geofence**: Remove toggle. Show as informational card: "Safe zones are created from the Parent Dashboard after setup."
- **"Continue"** button always enabled (validates PIN if enabled)
- **"Skip safety options"** button → shows confirmation dialog: "You can set these up later from Settings."

### 6. `WizardCompleteScreen` — Finish with pending items
**File**: `app/src/main/java/com/anchorguard/client/ui/screens/wizard/WizardCompleteScreen.kt`

**Changes:**
- Show **warning banner** if any item is pending: "Some items are not configured. You can set these up later from Settings."
- **Enable "Finish" button always** — don't block on incomplete items
- Keep summary rows with ✓/⚠ indicators
- "Review settings" → navigates back to relevant settings screen (or popBackStack)
- On Finish: mark `setup_wizard_completed = true` in `SecureSessionStore`

---

## Navigation Updates

**File**: `app/src/main/java/com/anchorguard/client/ui/nav/AnchorGuardNavHost.kt`

### New Routes (add to `Routes.kt`)
```kotlin
const val WizardEntry = "wizard_entry"
const val WizardChildRegister = "wizard_child_register"  // rename existing
const val WizardParentAuth = "wizard_parent_auth"        // existing
const val WizardPermissions = "wizard_permissions"       // existing
const val WizardPairing = "wizard_pairing"               // existing
const val WizardSafety = "wizard_safety"                 // existing
const val WizardComplete = "wizard_complete"             // existing
```

### Navigation Graph Changes
```kotlin
composable(Routes.WizardEntry) {
    WizardEntryScreen(
        onParentDevice = { navController.navigate(Routes.WizardParentAuth) },
        onChildDevice = { navController.navigate(Routes.WizardChildRegister + "?mode=child") },
        onParentAddingChild = { navController.navigate(Routes.WizardChildRegister + "?mode=parent_adding_child") }
    )
}

composable(
    route = "${Routes.WizardChildRegister}?mode={mode}",
    arguments = listOf(navArgument("mode") { type = NavType.StringType })
) { backStackEntry ->
    val mode = backStackEntry.getString() ?: "child"
    WizardChildRegisterScreen(
        setupMode = when (mode) { "parent_adding_child" -> SetupMode.PARENT_ADDING_CHILD else -> SetupMode.CHILD_DEVICE },
        onRegistered = { navController.navigate(Routes.WizardPermissions) { popUpTo(Routes.WizardChildRegister) { inclusive = true } } },
        onBack = { navController.popBackStack() }
    )
}

composable(Routes.WizardParentAuth) {
    WizardParentAuthScreen(
        onAuthenticated = { navController.navigate(Routes.WizardPermissions) { popUpTo(Routes.WizardParentAuth) { inclusive = true } } },
        onBack = { navController.popBackStack() }
    )
}

// Permissions, Pairing, Safety, Complete — same structure, just ensure popUpTo clears wizard stack on Finish
```

---

## Shared Components to Add

### 1. `WizardProgressIndicator` (composable)
**File**: `app/src/main/java/com/anchorguard/client/ui/components/WizardProgressIndicator.kt`

Horizontal step bar: "Step 2 of 6" with dots/lines. Takes `currentStep`, `totalSteps`, `stepLabels`.

### 2. `ExpandableHelpCard` (composable)
**File**: `app/src/main/java/com/anchorguard/client/ui/components/ExpandableHelpCard.kt`

Used in permissions screen — shows "How to grant" expandable section with numbered steps.

### 3. `SetupMode` enum
**File**: `app/src/main/java/com/anchorguard/client/ui/screens/wizard/SetupMode.kt`
```kotlin
enum class SetupMode { PARENT_DEVICE, CHILD_DEVICE, PARENT_ADDING_CHILD }
```

---

## Permission Guide Content (for ExpandableHelpCard)

| Permission | Steps |
|------------|-------|
| **Device Admin** | 1. Tap "Grant" → 2. Tap "Activate this device admin" → 3. Return to app |
| **Accessibility** | 1. Tap "Grant" → 2. Find "AnchorGuard" in list → 3. Toggle ON → 4. Allow → 5. Return |
| **Overlay** | 1. Tap "Grant" → 2. Toggle "Allow display over other apps" → 3. Return |
| **Battery** | 1. Tap "Grant" → 2. Find "AnchorGuard" → 3. Select "Allow" → 4. Return |

---

## Back Navigation Standardization

Every wizard screen gets a **top app bar with back arrow** (except `WizardEntryScreen`).
- Use `TopAppBar` with `NavigationIcon` = `Icons.Default.ArrowBack`
- `onBack` callback passed to each screen
- Consistent placement, consistent behavior

---

## Splash Screen Integration

**File**: `app/src/main/java/com/anchorguard/client/ui/screens/SplashScreen.kt`

Change `onWizardNeeded` to navigate to `Routes.WizardEntry` (instead of `Routes.WizardWelcome`).

---

## SecureSessionStore Updates

**File**: `app/src/main/java/com/anchorguard/client/auth/security/SecureSessionStore.kt`

- Add `setupWizardCompleted()` / `setSetupWizardCompleted()` (already exists)
- Add `wizardSetupMode()` / `setWizardSetupMode(mode: SetupMode)` for analytics/debugging
- On `WizardCompleteScreen.onFinish`: call `setSetupWizardCompleted(true)`

---

## Testing Checklist

### Path A: Parent Device
- [ ] Entry → Parent Auth (sign in) → Permissions → Pairing → Safety → Complete → Parent Dashboard
- [ ] Entry → Parent Auth (create account) → same
- [ ] Back navigation works at every step
- [ ] Permissions auto-detect grant where possible
- [ ] Pairing code generates, can be copied, input field works
- [ ] PIN required, SOS/geofence optional
- [ ] Finish works with pending items (warning shown)

### Path B: Child Device
- [ ] Entry → Child Register (auto) → Permissions → Pairing (show code only) → Safety → Complete → Child Home
- [ ] Pairing code shown, no input field needed (but present)
- [ ] PIN optional, "Set up PIN later" works
- [ ] Parent enters code on their device → child appears in dashboard

### Path C: Parent Adding Child
- [ ] Entry → Child Register (parent_adding_child mode) → same as Path B
- [ ] No auth on this device
- [ ] Parent enters code on their already-authenticated device

### Edge Cases
- [ ] Network failure during registration → retry button works
- [ ] Permission denied → user can still skip, warning shown
- [ ] App killed mid-wizard → resume at correct screen (navigation stack preserved)
- [ ] Rotation/config change → state preserved (rememberSaveable)
- [ ] Already completed wizard → splash skips to appropriate home screen

---

## File List Summary

### New Files (4)
```
app/src/main/java/com/anchorguard/client/ui/screens/wizard/WizardEntryScreen.kt
app/src/main/java/com/anchorguard/client/ui/components/WizardProgressIndicator.kt
app/src/main/java/com/anchorguard/client/ui/components/ExpandableHelpCard.kt
app/src/main/java/com/anchorguard/client/ui/screens/wizard/SetupMode.kt
```

### Modified Files (8)
```
app/src/main/java/com/anchorguard/client/ui/nav/Routes.kt
app/src/main/java/com/anchorguard/client/ui/nav/AnchorGuardNavHost.kt
app/src/main/java/com/anchorguard/client/ui/screens/wizard/WizardChildRegisterScreen.kt
app/src/main/java/com/anchorguard/client/ui/screens/wizard/WizardPermissionGuideScreen.kt
app/src/main/java/com/anchorguard/client/ui/screens/wizard/WizardPairingScreen.kt
app/src/main/java/com/anchorguard/client/ui/screens/wizard/WizardSafetyOptionsScreen.kt
app/src/main/java/com/anchorguard/client/ui/screens/wizard/WizardCompleteScreen.kt
app/src/main/java/com/anchorguard/client/ui/screens/SplashScreen.kt
```

### Potentially Touched (2)
```
app/src/main/java/com/anchorguard/client/auth/security/SecureSessionStore.kt
app/src/main/java/com/anchorguard/client/ui/screens/wizard/WizardParentAuthScreen.kt (minor: back nav)
```

---

## Effort Estimate

| Task | Estimate |
|------|----------|
| WizardEntryScreen + SetupMode | 2-3 hrs |
| WizardProgressIndicator + ExpandableHelpCard | 2 hrs |
| Permissions screen overhaul (guide, progress, skip) | 4-5 hrs |
| Pairing screen unification | 2-3 hrs |
| Safety screen reorder + optional PIN | 2 hrs |
| Complete screen finish-with-pending | 1-2 hrs |
| Navigation graph updates | 1 hr |
| Back navigation standardization (all screens) | 2 hrs |
| Splash integration | 0.5 hr |
| Testing all 3 paths + edge cases | 4-6 hrs |
| **Total** | **~21-28 hrs** |

---

## Priority Order (if phased)

1. **Phase 1 (Core flow)**: WizardEntryScreen, navigation changes, ChildRegister mode, Splash integration
2. **Phase 2 (Permissions UX)**: Progress indicator, guided help, skip option
3. **Phase 3 (Pairing & Safety)**: Unified pairing, safety reorder, optional PIN
4. **Phase 4 (Complete & Polish)**: Finish-with-pending, back nav standardization, testing

---

## Risks & Mitigations

| Risk | Mitigation |
|------|------------|
| Permission auto-detection unreliable on some Android versions | Keep manual "I've granted" button as fallback; test on API 29-36 |
| Pairing code expiry too short | Configurable via backend; default 10 min, show countdown |
| User skips permissions → enforcement broken | Warning banner on Complete; enforcement service shows notification if perms missing |
| Back stack pollution | Use `popUpTo(WizardEntry) { inclusive = true }` on Finish; `popUpTo(current) { inclusive = true }` on forward nav |

---

## Success Metrics

- Wizard completion rate > 85% (from ~60% estimated)
- Support tickets for "how to grant permissions" reduced by 50%
- Time-to-complete < 3 minutes for Path B/C, < 5 minutes for Path A
- Zero crashes during wizard flow
