package com.anchorguard.client.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.anchorguard.client.enforcement.EnforcementPermissionChecker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Button

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionGuideScreen(onAllGranted: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val checker = remember { EnforcementPermissionChecker(context) }

    val deviceAdminState = remember { mutableStateOf(false) }
    val accessibilityState = remember { mutableStateOf(false) }
    val overlayState = remember { mutableStateOf(false) }
    val batteryOptState = remember { mutableStateOf(false) }
    val isCheckingState = remember { mutableStateOf(false) }

    fun refreshStatus() {
        isCheckingState.value = true
        scope.launch {
            val admin = withContext(Dispatchers.Default) { checker.isDeviceAdminActive() }
            val access = withContext(Dispatchers.Default) { checker.isAccessibilityServiceEnabled() }
            val overl = withContext(Dispatchers.Default) { checker.isOverlayPermissionGranted() }
            val batt = withContext(Dispatchers.Default) { checker.isBatteryOptimizationExempt() }
            deviceAdminState.value = admin
            accessibilityState.value = access
            overlayState.value = overl
            batteryOptState.value = batt
            isCheckingState.value = false

            if (admin && access && overl && batt) {
                scope.launch {
                    onAllGranted()
                }
            }
        }
    }

    val allGranted = deviceAdminState.value && accessibilityState.value && overlayState.value && batteryOptState.value

    LaunchedEffect(Unit) {
        refreshStatus()
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Enforcement Permissions") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                ),
                navigationIcon = {
                    androidx.compose.material3.IconButton(onClick = { /* handled by nav host */ }) {
                        Icon(
                            painter = ColorPainter(MaterialTheme.colorScheme.primary),
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Grant permissions for full device protection",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "AnchorGuard needs these system permissions to enforce app blocking, schedules, and remote lock.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))

            PermissionCard(
                title = "Device Admin",
                description = "Allows AnchorGuard to enforce app blocking, remote device lock, and prevent uninstallation.",
                granted = deviceAdminState.value,
                iconPainter = if (deviceAdminState.value) ColorPainter(MaterialTheme.colorScheme.primary) else ColorPainter(MaterialTheme.colorScheme.error),
                onGrant = { checker.requestDeviceAdmin() }
            )
            PermissionCard(
                title = "Accessibility Service",
                description = "Monitors foreground apps so AnchorGuard can block apps when they come to the front.",
                granted = accessibilityState.value,
                iconPainter = if (accessibilityState.value) ColorPainter(MaterialTheme.colorScheme.primary) else ColorPainter(MaterialTheme.colorScheme.error),
                onGrant = { checker.requestAccessibility() }
            )
            PermissionCard(
                title = "Overlay Windows",
                description = "Shows the block screen when a blocked app is opened.",
                granted = overlayState.value,
                iconPainter = if (overlayState.value) ColorPainter(MaterialTheme.colorScheme.primary) else ColorPainter(MaterialTheme.colorScheme.error),
                onGrant = { checker.requestOverlay() }
            )
            PermissionCard(
                title = "Battery Optimization",
                description = "Exempts AnchorGuard from battery restrictions so enforcement runs reliably in the background.",
                granted = batteryOptState.value,
                iconPainter = if (batteryOptState.value) ColorPainter(MaterialTheme.colorScheme.primary) else ColorPainter(MaterialTheme.colorScheme.error),
                onGrant = { checker.requestBatteryOptimization() }
            )

            Spacer(Modifier.height(16.dp))

            if (isCheckingState.value) {
                CircularProgressIndicator(modifier = Modifier.fillMaxWidth().align(Alignment.CenterHorizontally))
            } else if (!allGranted) {
                Button(
                    onClick = { refreshStatus() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Re-check Permissions")
                }
            } else {
                Button(
                    onClick = { onAllGranted() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(text = "All Permissions Granted — Continue")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PermissionCard(
    title: String,
    description: String,
    granted: Boolean,
    iconPainter: androidx.compose.ui.graphics.painter.Painter,
    onGrant: () -> Unit
) {
    androidx.compose.material3.Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        elevation = androidx.compose.material3.CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        androidx.compose.foundation.layout.Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = iconPainter,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
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
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.width(180.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            androidx.compose.material3.TextButton(
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