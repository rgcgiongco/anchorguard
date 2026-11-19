package com.anchorguard.client.ui.screens

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.anchorguard.client.ui.components.AnchorInfoCard
import com.anchorguard.client.ui.components.AnchorPrimaryButton
import com.anchorguard.client.ui.components.AnchorSecondaryButton
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme

@Composable
fun ParentDashboardScreen(
    onDeviceSettings: () -> Unit,
    onSecurity: () -> Unit,
    onUsers: () -> Unit,
    onTimer: () -> Unit,
    onAccount: () -> Unit,
    enforcementActive: Boolean = true
) {
    ScreenScaffold(
        title = "Dashboard",
        subtitle = "Manage linked devices and quick controls."
    ) {
        if (!enforcementActive) {
            androidx.compose.material3.Card(
                modifier = Modifier.fillMaxWidth(),
                colors = androidx.compose.material3.CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                ),
                elevation = androidx.compose.material3.CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                androidx.compose.foundation.layout.Column(
                    modifier = androidx.compose.foundation.layout Modifier.padding(12.dp),
                    verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Enforcement permissions not granted",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Text(
                        text = "App blocking, remote lock, and schedules will not work until Device Admin, Accessibility, Overlay, and Battery Optimization are granted.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
        }
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
