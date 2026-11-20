package com.anchorguard.client.ui.screens.wizard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Warning
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LifecycleEventObserver
import kotlinx.coroutines.launch
import com.anchorguard.client.auth.security.SecureSessionStore
import com.anchorguard.client.enforcement.EnforcementPermissionChecker
import com.anchorguard.client.ui.components.AnchorChip
import com.anchorguard.client.ui.components.AnchorPrimaryButton
import com.anchorguard.client.ui.components.AnchorSecondaryButton
import com.anchorguard.client.ui.components.ExpandableHelpCard
import com.anchorguard.client.ui.components.WizardProgressIndicator
import com.anchorguard.client.ui.screens.ScreenScaffold
import androidx.compose.runtime.rememberUpdatedState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WizardPermissionGuideScreen(
    onAllGranted: () -> Unit,
    onSkipped: () -> Unit,
    onBack: () -> Unit,
    currentStep: Int = 3,
    totalSteps: Int = 6
) {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val scope = rememberCoroutineScope()
    val checker = remember { EnforcementPermissionChecker(context) }
    val store = remember { SecureSessionStore(context) }

    var deviceAdmin by remember { mutableStateOf(false) }
    var accessibility by remember { mutableStateOf(false) }
    var overlay by remember { mutableStateOf(false) }
    var batteryOpt by remember { mutableStateOf(false) }
    var refreshKey by remember { mutableIntStateOf(0) }
    var skipDialogOpen by remember { mutableStateOf(false) }
    var permissionsSkipped by remember { mutableStateOf(false) }

    fun refreshStatus() {
        scope.launch {
            deviceAdmin = checker.isDeviceAdminActive()
            accessibility = checker.isAccessibilityServiceEnabled()
            overlay = checker.isOverlayPermissionGranted()
            batteryOpt = checker.isBatteryOptimizationExempt()
            store.setDeviceAdminGranted(deviceAdmin)
            store.setAccessibilityGranted(accessibility)
            store.setOverlayGranted(overlay)
            store.setBatteryOptOut(batteryOpt)
        }
    }

    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                refreshKey += 1
            }
        }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(refreshKey) {
        refreshStatus()
    }

    val allGranted = deviceAdmin && accessibility && overlay && batteryOpt

    ScreenScaffold(
        title = "Enforcement permissions",
        subtitle = "AnchorGuard needs these system permissions to enforce app blocking, schedules, and remote lock.",
        navigationIcon = {
            IconButton(onClick = onBack) {
                    Text(text = "\u2190", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
                }
        }
    ) {
        WizardProgressIndicator(currentStep = currentStep, totalSteps = totalSteps)
        Spacer(Modifier.height(16.dp))

        val role = store.deviceRole()
        if (role == "both") {
            ExpandableHelpCard(
                title = "Shared device — Parent + Child",
                initiallyExpanded = true,
                content = {
                    Column {
                        Text(
                            text = "This device is set up for shared use. Parent Mode gives full access to all apps. Child Mode restricts apps and schedules." + 
                            "\n\nSwitch between modes using the Parent PIN. Enforcement only runs in Child Mode.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            )
            Spacer(Modifier.height(16.dp))
        }

        ExpandableHelpCard(
            title = "Why these permissions matter",
            initiallyExpanded = false,
            content = {
            Column {
                Text(
                    text = "Without these permissions, AnchorGuard cannot block apps, enforce schedules, or remotely lock the device. Grant each permission by tapping its “Grant” button and following the system prompt.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "You can return to this screen later from Settings if you skip now, but enforcement will not work until all permissions are granted.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        )
        Spacer(Modifier.height(16.dp))

        PermissionCard(
            title = "Device Admin",
            description = "Enforce app blocking, remote device lock, and prevent uninstallation.",
            granted = deviceAdmin,
            iconContent = {
                    if (deviceAdmin) Text(text = "\u2713", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                    else Text(text = "\u26A0", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.error)
                },
            onGrant = { checker.requestDeviceAdmin(); refreshKey += 1 }
        )
        Spacer(Modifier.height(12.dp))
        PermissionCard(
            title = "Accessibility Service",
            description = "Monitor foreground apps so blocked apps can be closed when they come to the front.",
            granted = accessibility,
            iconContent = {
                    if (accessibility) Text(text = "\u2713", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                    else Text(text = "\u26A0", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.error)
                },
            onGrant = { checker.requestAccessibility(); refreshKey += 1 }
        )
        Spacer(Modifier.height(12.dp))
        PermissionCard(
            title = "Overlay Windows",
            description = "Show the block screen when a blocked app is opened.",
            granted = overlay,
            iconContent = {
                    if (overlay) Text(text = "\u2713", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                    else Text(text = "\u26A0", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.error)
                },
            onGrant = { checker.requestOverlay(); refreshKey += 1 }
        )
        Spacer(Modifier.height(12.dp))
        PermissionCard(
            title = "Battery Optimization",
            description = "Exempt AnchorGuard from battery restrictions so enforcement runs in the background.",
            granted = batteryOpt,
            iconContent = {
                    if (batteryOpt) Text(text = "\u2713", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                    else Text(text = "\u26A0", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.error)
                },
            onGrant = { checker.requestBatteryOptimization(); refreshKey += 1 }
        )
        Spacer(Modifier.height(24.dp))

        AnchorPrimaryButton(
            text = "I've granted all permissions",
            onClick = {
                if (allGranted) {
                    if (store.isBothRole()) {
                        store.setActiveMode("parent")
                    }
                    onAllGranted()
                }
            },
            enabled = allGranted
        )
        Spacer(Modifier.height(8.dp))

        if (permissionsSkipped) {
            Surface(
                color = MaterialTheme.colorScheme.errorContainer,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Warning,
                            contentDescription = "Warning",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Enforcement will not work until all permissions are granted.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
        }

        AnchorSecondaryButton(
            text = "Skip permissions (not recommended)",
            onClick = { skipDialogOpen = true },
            enabled = !allGranted
        )
        Spacer(Modifier.height(8.dp))
        AnchorSecondaryButton(text = "Back", onClick = onBack)

        AlertDialog(
            onDismissRequest = { skipDialogOpen = false },
            title = { Text(text = "Skip permissions?") },
            text = {
                Text(
                    text = "Without these permissions, AnchorGuard cannot block apps, enforce schedules, or remotely lock this device. You can grant them later from Settings."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        permissionsSkipped = true
                        store.setDeviceAdminGranted(false)
                        store.setAccessibilityGranted(false)
                        store.setOverlayGranted(false)
                        store.setBatteryOptOut(false)
                        skipDialogOpen = false
                        onSkipped()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(text = "Yes, skip")
                }
            },
            dismissButton = {
                TextButton(onClick = { skipDialogOpen = false }) {
                    Text(text = "Cancel")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PermissionCard(
    title: String,
    description: String,
    granted: Boolean,
    iconContent: @Composable () -> Unit,
    onGrant: () -> Unit
) {
    Card(
        modifier = androidx.compose.ui.Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        androidx.compose.foundation.layout.Row(
            modifier = androidx.compose.ui.Modifier.padding(12.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = androidx.compose.ui.Modifier.size(24.dp),
                contentAlignment = Alignment.Center
            ) {
                iconContent()
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (granted) "Granted" else "Pending",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (granted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            AnchorChip(
                label = if (granted) "Granted" else "Pending",
                selected = granted,
                onClick = {}
            )
            Spacer(modifier = Modifier.width(8.dp))
            TextButton(
                onClick = onGrant,
                enabled = !granted
            ) {
                Text(
                    text = if (granted) "Done" else "Grant",
                    style = MaterialTheme.typography.labelLarge,
                    color = if (granted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
