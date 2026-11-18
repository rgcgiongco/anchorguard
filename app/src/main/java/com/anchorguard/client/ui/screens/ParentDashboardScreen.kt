package com.anchorguard.client.ui.screens

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.anchorguard.client.ui.components.AnchorInfoCard
import com.anchorguard.client.ui.components.AnchorPrimaryButton
import com.anchorguard.client.ui.components.AnchorSecondaryButton

@Composable
fun ParentDashboardScreen(
    onDeviceSettings: () -> Unit,
    onSecurity: () -> Unit,
    onUsers: () -> Unit,
    onTimer: () -> Unit,
    onAccount: () -> Unit
) {
    ScreenScaffold(
        title = "Dashboard",
        subtitle = "Manage linked devices and quick controls."
    ) {
        AnchorInfoCard(title = "Devices", body = "Linked child devices and status will appear here.")
        Spacer(Modifier.height(12.dp))
        AnchorPrimaryButton(text = "Device settings", onClick = onDeviceSettings)
        Spacer(Modifier.height(8.dp))
        AnchorSecondaryButton(text = "Screen time & timer", onClick = onTimer)
        Spacer(Modifier.height(8.dp))
        AnchorSecondaryButton(text = "Users", onClick = onUsers)
        Spacer(Modifier.height(8.dp))
        AnchorSecondaryButton(text = "Security", onClick = onSecurity)
        Spacer(Modifier.height(8.dp))
        AnchorSecondaryButton(text = "Account", onClick = onAccount)
    }
}
