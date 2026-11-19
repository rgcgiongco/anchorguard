package com.anchorguard.client.ui.screens

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import com.anchorguard.client.data.location.LocationTrackingController
import com.anchorguard.client.ui.components.AnchorInfoCard
import com.anchorguard.client.ui.components.AnchorSwitch
import kotlinx.coroutines.launch

@Composable
fun DeviceSettingsScreen() {
    val context = LocalContext.current
    val controller = remember { LocationTrackingController.init(context) }
    val scope = rememberCoroutineScope()
    var trackingEnabled by remember { mutableStateOf(false) }

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
        AnchorInfoCard(title = "App allow list", body = "Approve or block installed apps (Module 04/05 integration).")
        Spacer(Modifier.height(12.dp))
        AnchorInfoCard(title = "Schedules", body = "Daily/weekly schedules and bedtime windows placeholder.")
        Spacer(Modifier.height(12.dp))
        AnchorInfoCard(title = "Per-app limits", body = "Time limits and internet toggles per app placeholder.")
    }
}
