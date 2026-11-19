package com.anchorguard.client.ui.screens.wizard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch
import androidx.lifecycle.Lifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LifecycleEventObserver
import com.anchorguard.client.auth.security.SecureSessionStore
import com.anchorguard.client.enforcement.EnforcementPermissionChecker
import com.anchorguard.client.ui.components.AnchorChip
import com.anchorguard.client.ui.components.AnchorPrimaryButton
import com.anchorguard.client.ui.components.AnchorSecondaryButton
import com.anchorguard.client.ui.screens.ScreenScaffold
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Cast
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.BatterySaver

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WizardPermissionGuideScreen(
    onAllGranted: () -> Unit,
    onBack: () -> Unit
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
            if (event == Lifecycle.Event.ON_RESUME) {
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
                Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
            }
        }
    ) {
        PermissionCard(
            title = "Device Admin",
            description = "Enforce app blocking, remote device lock, and prevent uninstallation.",
            granted = deviceAdmin,
            icon = if (deviceAdmin) Icons.Filled.DoneAll else Icons.Filled.Warning,
            onGrant = { checker.requestDeviceAdmin(); refreshKey += 1 }
        )
        Spacer(Modifier.height(12.dp))
        PermissionCard(
            title = "Accessibility Service",
            description = "Monitor foreground apps so blocked apps can be closed when they come to the front.",
            granted = accessibility,
            icon = if (accessibility) Icons.Filled.DoneAll else Icons.Filled.Warning,
            onGrant = { checker.requestAccessibility(); refreshKey += 1 }
        )
        Spacer(Modifier.height(12.dp))
        PermissionCard(
            title = "Overlay Windows",
            description = "Show the block screen when a blocked app is opened.",
            granted = overlay,
            icon = if (overlay) Icons.Filled.DoneAll else Icons.Filled.Warning,
            onGrant = { checker.requestOverlay(); refreshKey += 1 }
        )
        Spacer(Modifier.height(12.dp))
        PermissionCard(
            title = "Battery Optimization",
            description = "Exempt AnchorGuard from battery restrictions so enforcement runs in the background.",
            granted = batteryOpt,
            icon = if (batteryOpt) Icons.Filled.DoneAll else Icons.Filled.Warning,
            onGrant = { checker.requestBatteryOptimization(); refreshKey += 1 }
        )
        Spacer(Modifier.height(24.dp))
        AnchorPrimaryButton(
            text = "I've granted all permissions",
            onClick = {
                if (allGranted) onAllGranted()
            },
            enabled = allGranted
        )
        Spacer(Modifier.height(8.dp))
        AnchorSecondaryButton(text = "Back", onClick = onBack)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PermissionCard(
    title: String,
    description: String,
    granted: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
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
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (granted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                modifier = androidx.compose.ui.Modifier.size(24.dp)
            )
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
