package com.anchorguard.client.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.anchorguard.client.ui.components.AnchorPrimaryButton
import com.anchorguard.client.ui.components.AnchorSecondaryButton
import com.anchorguard.client.ui.components.DeviceCard
import com.anchorguard.client.ui.model.Device
import com.anchorguard.client.ui.screens.ScreenScaffold
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.SignalWifi4Bar
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Schedule

/**
 * Parent dashboard with stats grid and device list.
 * Provides quick controls for device management and enforcement.
 */
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
                    modifier = Modifier.padding(12.dp),
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

        // Stats row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatCard(
                label = "Linked Devices",
                value = "1"
            )
            Spacer(modifier = Modifier.width(8.dp))
            StatCard(
                label = "Online",
                value = "1"
            )
            Spacer(modifier = Modifier.width(8.dp))
            StatCard(
                label = "Enforcement",
                value = if (enforcementActive) "Active" else "Inactive"
            )
            Spacer(modifier = Modifier.width(8.dp))
            StatCard(
                label = "Schedules",
                value = "0"
            )
        }
        Spacer(Modifier.height(24.dp))

        // Device list
        androidx.compose.foundation.layout.Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Devices",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(12.dp))

            DeviceCard(
                device = com.anchorguard.client.ui.model.Device(
                    id = "device-1",
                    name = "Child Device",
                    model = "Android Phone",
                    isOnline = true,
                    lastSeen = null,
                    isPaired = true,
                    enforcementActive = enforcementActive
                ),
                onSettings = onDeviceSettings,
                onEnforcementToggle = { /* handled by parent */ }
            )
        }

        Spacer(Modifier.height(32.dp))

        // Quick actions
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
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
}

@Composable
private fun StatCard(
    label: String,
    value: String
) {
    Card(
        modifier = Modifier.fillMaxWidth().height(80.dp),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
