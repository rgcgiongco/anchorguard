package com.anchorguard.client.ui.screens.wizard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.anchorguard.client.auth.data.AuthRepository
import com.anchorguard.client.auth.security.PinSecurityManager
import com.anchorguard.client.auth.security.SecureSessionStore
import com.anchorguard.client.ui.components.AnchorPrimaryButton
import com.anchorguard.client.ui.components.AnchorSecondaryButton
import com.anchorguard.client.ui.screens.ScreenScaffold

@Composable
fun WizardCompleteScreen(
    onFinish: () -> Unit,
    onReview: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val repo = remember { AuthRepository(context) }
    val store = remember { SecureSessionStore(context) }
    val pinManager = remember { PinSecurityManager(context) }

    val deviceRegistered = repo.deviceId() != null && repo.deviceId().isNotBlank()
    val permissionsGranted = store.allWizardPermissionsGranted()
    val paired = store.isDevicePaired()
    val pinSet = pinManager.isPinSet()
    val sosEnabled = store.isSosEnabled()
    val geofenceEnabled = store.isGeofenceEnabled()
    val role = store.wizardRole()

    ScreenScaffold(
        title = "Setup complete",
        subtitle = "AnchorGuard is ready to protect this device."
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = androidx.compose.ui.Alignment.Start
        ) {
            Text(
                text = "Setup complete!",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Here's what was configured on this device:",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))

            SummaryRow("Device registered", deviceRegistered)
            SummaryRow("Enforcement permissions", permissionsGranted)
            SummaryRow("Device paired", paired)
            SummaryRow("Parent PIN", pinSet)
            SummaryRow("SOS emergency", sosEnabled)
            SummaryRow("Safe zones (geofence)", geofenceEnabled)

            Spacer(Modifier.height(24.dp))

            AnchorPrimaryButton(
                text = "Finish",
                onClick = onFinish,
                modifier = Modifier.fillMaxWidth(),
                enabled = deviceRegistered && permissionsGranted && paired
            )
            Spacer(Modifier.height(8.dp))
            RowSummaryActions(onReview, onBack)
        }
    }

    if (role != null) {
        androidx.compose.material3.Text(
            text = "Role: ${role.capitalize()}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Composable
private fun SummaryRow(label: String, ok: Boolean) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
    ) {
        Text(
            text = if (ok) "\u2713" else "\u26A0",
            color = if (ok) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = if (ok) "Configured" else "Pending",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun RowSummaryActions(onReview: () -> Unit, onBack: () -> Unit) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
    ) {
        AnchorSecondaryButton(text = "Review settings", onClick = onReview)
        AnchorSecondaryButton(text = "Back", onClick = onBack)
    }
}
