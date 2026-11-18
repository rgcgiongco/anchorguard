package com.anchorguard.client.ui.screens

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.anchorguard.client.enforcement.EnforcementController
import com.anchorguard.client.enforcement.EnforcementState
import com.anchorguard.client.ui.components.AnchorInfoCard
import com.anchorguard.client.ui.components.AnchorSecondaryButton

@Composable
fun ChildHomeScreen(onOpenParentGate: () -> Unit) {
    val context = LocalContext.current
    val state by EnforcementController.state.collectAsState()
    val fg by EnforcementController.foregroundPackage.collectAsState()

    ScreenScaffold(
        title = "Child home",
        subtitle = "Launcher and protection status. Enforcement engine runs locally (Module 04)."
    ) {
        AnchorInfoCard(
            title = "Protection status",
            body = "State: ${when (state) {
                EnforcementState.IDLE -> "Idle"
                EnforcementState.RUNNING -> "Running"
                EnforcementState.BLOCKED -> "Blocked"
                EnforcementState.PARENT_MODE -> "Parent mode"
            }}${if (!fg.isNullOrBlank()) "\nForeground: $fg" else ""}"
        )
        Spacer(Modifier.height(12.dp))
        AnchorInfoCard(
            title = "Allowed apps",
            body = "A grid of approved apps and categories will appear here. Blocked apps trigger the overlay when protection is running."
        )
        Spacer(Modifier.height(12.dp))
        AnchorInfoCard(
            title = "Time remaining",
            body = "Today's remaining screen time will be shown here."
        )
        Spacer(Modifier.height(20.dp))
        AnchorSecondaryButton(text = "Parent access", onClick = onOpenParentGate)
        Spacer(Modifier.height(8.dp))
        if (state == EnforcementState.PARENT_MODE) {
            AnchorSecondaryButton(
                text = "Exit parent mode",
                onClick = { EnforcementController.exitParentMode(context) }
            )
        } else {
            Text("Use Security screen to start/stop protection and grant required permissions.")
        }
    }
}
