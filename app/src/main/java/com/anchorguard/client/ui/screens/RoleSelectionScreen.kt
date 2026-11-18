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
fun RoleSelectionScreen(
    onParentDevice: () -> Unit,
    onChildDevice: () -> Unit
) {
    ScreenScaffold(
        title = "Device role",
        subtitle = "Choose how this device will be used."
    ) {
        AnchorInfoCard(
            title = "Parent device",
            body = "Manage family settings, review devices, and control pairing."
        )
        Spacer(Modifier.height(12.dp))
        AnchorPrimaryButton(text = "Use as parent device", onClick = onParentDevice)
        Spacer(Modifier.height(20.dp))
        AnchorInfoCard(
            title = "Child device",
            body = "Apply allowed apps, schedules, and screen-time rules on this device."
        )
        Spacer(Modifier.height(12.dp))
        AnchorSecondaryButton(text = "Use as child device", onClick = onChildDevice)
    }
}
