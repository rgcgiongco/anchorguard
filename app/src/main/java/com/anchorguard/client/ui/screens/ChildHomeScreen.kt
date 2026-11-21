package com.anchorguard.client.ui.screens

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.anchorguard.client.enforcement.EnforcementController
import com.anchorguard.client.enforcement.EnforcementState
import com.anchorguard.client.ui.components.AllowedAppsGrid
import com.anchorguard.client.ui.components.AppGridCard
import com.anchorguard.client.ui.components.AllowedApp
import com.anchorguard.client.ui.components.BottomBar
import com.anchorguard.client.ui.components.ChildTopBar
import com.anchorguard.client.ui.components.PinVerificationDialog
import com.anchorguard.client.ui.screens.ScreenScaffold
import com.anchorguard.client.ui.theme.KidTheme
import com.anchorguard.client.ui.theme.getKidThemeColorScheme
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton

/**
 * Child Home Screen - Full-screen launcher (Kiosk Mode).
 *
 * Shows a grid of allowed apps that the child can launch.
 * Top bar has Settings ⚙ and Exit ✕ (both PIN-protected).
 * Bottom bar has SOS emergency button and time remaining.
 *
 * In Parent Mode: full app access, can switch back to Child Mode.
 * In Child Mode: restricted to allowed apps only.
 */
@Composable
fun ChildHomeScreen(
    kidTheme: KidTheme = KidTheme.OCEAN_BLUE,
    childName: String = "Child",
    onOpenParentGate: () -> Unit,
    onOpenSos: () -> Unit,
    onOpenSettings: () -> Unit,
    onExitChildMode: () -> Unit,
    onSwitchToParentMode: () -> Unit,
    onSwitchToChildMode: () -> Unit,
    isSharedDevice: Boolean = false,
    isParentMode: Boolean = false
) {
    val context = LocalContext.current
    val colorScheme = getKidThemeColorScheme(kidTheme)
    var showPinDialog by remember { mutableStateOf(false) }
    var pinDialogMode by remember { mutableStateOf<PinDialogMode?>(null) }

    // Mock allowed apps - in production this comes from DevicePolicyRepository
    val allowedApps = listOf(
        AllowedApp("com.android.chrome", "Browser"),
        AllowedApp("com.android.contacts", "Contacts"),
        AllowedApp("com.android.camera", "Camera"),
        AllowedApp("com.android.gallery3d", "Gallery"),
        AllowedApp("com.android.calculator2", "Calculator"),
        AllowedApp("com.android.settings", "Settings"),
        AllowedApp("com.android.music", "Music"),
        AllowedApp("com.android.clock", "Clock")
    )
    val timeRemainingMap = mapOf(
        "com.android.chrome" to 45L * 60000,
        "com.android.contacts" to 0L,
        "com.android.camera" to 120L * 60000,
        "com.android.gallery3d" to 0L,
        "com.android.calculator2" to 0L,
        "com.android.settings" to 0L,
        "com.android.music" to 90L * 60000,
        "com.android.clock" to 0L
    )

    val totalTimeRemaining = timeRemainingMap.values.sum()

    ScreenScaffold(
        title = "",
        subtitle = "",
        navigationIcon = {
            IconButton(onClick = {
                showPinDialog = true
                pinDialogMode = PinDialogMode.Settings
            }) {
                Icon(imageVector = Icons.Filled.Settings, contentDescription = "Settings")
            }
        },
        actions = {
            IconButton(onClick = {
                showPinDialog = true
                pinDialogMode = PinDialogMode.Exit
            }) {
                Icon(imageVector = Icons.Filled.Close, contentDescription = "Exit")
            }
        },
        bottomBar = {
            BottomBar(
                totalTimeRemainingMillis = totalTimeRemaining,
                kidTheme = kidTheme,
                onSosClick = onOpenSos,
                onParentModeClick = {
                    showPinDialog = true
                    pinDialogMode = PinDialogMode.ParentMode
                },
                showParentModeToggle = isSharedDevice
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(0.dp)
        ) {
            // Main content - scrollable app grid
            AllowedAppsGrid(
                apps = allowedApps,
                timeRemainingMap = timeRemainingMap,
                kidTheme = kidTheme,
                onAppClick = { pkg ->
                    EnforcementController.syncAppDailyLimit(context, pkg, null)
                }
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // PIN Verification Dialog
    if (showPinDialog) {
        PinVerificationDialog(
            title = when (pinDialogMode) {
                PinDialogMode.Settings -> "Parent Settings"
                PinDialogMode.Exit -> "Exit Child Mode"
                PinDialogMode.ParentMode -> "Switch to Parent Mode"
                null -> "Parent Settings"
            },
            subtitle = "Enter the parent PIN to continue",
            onVerified = {
                showPinDialog = false
                when (pinDialogMode) {
                    PinDialogMode.Settings -> onOpenSettings()
                    PinDialogMode.Exit -> onExitChildMode()
                    PinDialogMode.ParentMode -> onSwitchToParentMode()
                    null -> { /* no-op */ }
                }
            },
            onDismiss = {
                showPinDialog = false
                pinDialogMode = null
            }
        )
    }
}

private enum class PinDialogMode {
    Settings, Exit, ParentMode
}
