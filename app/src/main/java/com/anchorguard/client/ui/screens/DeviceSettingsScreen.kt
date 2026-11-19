package com.anchorguard.client.ui.screens

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import com.anchorguard.client.auth.data.AuthRepository
import com.anchorguard.client.data.location.LocationTrackingController
import com.anchorguard.client.ui.components.AnchorButton
import com.anchorguard.client.ui.components.AnchorSwitch
import com.anchorguard.client.ui.screens.ScreenScaffold
import kotlinx.coroutines.launch

@Composable
fun DeviceSettingsScreen(
    deviceId: String? = null,
    onOpenAppList: () -> Unit = {},
    onOpenSchedules: () -> Unit = {},
    onOpenPerAppLimits: () -> Unit = {}
) {
    val context = LocalContext.current
    val authRepo = remember { AuthRepository(context) }
    val controller = remember { LocationTrackingController.init(context) }
    val scope = rememberCoroutineScope()
    var trackingEnabled by remember { mutableStateOf(false) }
    val effectiveDeviceId = remember(deviceId) { deviceId ?: authRepo.deviceId() ?: "" }

    LaunchedEffect(Unit) {
        scope.launch {
            trackingEnabled = controller.isTrackingEnabled()
        }
    }

    LaunchedEffect(trackingEnabled) {
        scope.launch {
            controller.setTrackingEnabled(trackingEnabled)
        }
    }

    ScreenScaffold(
        title = "Device settings",
        subtitle = "Configure allowed apps, schedules, and per-app limits."
    ) {
        AnchorSwitch(
            checked = trackingEnabled,
            onCheckedChange = { trackingEnabled = it },
            label = "Location tracking",
            description = "Share device location with your parent console for safety and geofences.",
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))
        AnchorButton(
            text = "App allow list",
            onClick = onOpenAppList,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))
        AnchorButton(
            text = "Schedules",
            onClick = onOpenSchedules,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))
        AnchorButton(
            text = "Per-app limits",
            onClick = onOpenPerAppLimits,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
