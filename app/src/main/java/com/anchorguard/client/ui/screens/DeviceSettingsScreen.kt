package com.anchorguard.client.ui.screens

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.anchorguard.client.ui.components.AnchorInfoCard

@Composable
fun DeviceSettingsScreen() {
    ScreenScaffold(
        title = "Device settings",
        subtitle = "Configure allowed apps, schedules, and per-app limits."
    ) {
        AnchorInfoCard(title = "App allow list", body = "Approve or block installed apps (Module 04/05 integration).")
        Spacer(Modifier.height(12.dp))
        AnchorInfoCard(title = "Schedules", body = "Daily/weekly schedules and bedtime windows placeholder.")
        Spacer(Modifier.height(12.dp))
        AnchorInfoCard(title = "Per-app limits", body = "Time limits and internet toggles per app placeholder.")
    }
}
