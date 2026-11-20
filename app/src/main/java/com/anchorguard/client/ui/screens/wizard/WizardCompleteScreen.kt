package com.anchorguard.client.ui.screens.wizard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.material.icons.Icons
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.anchorguard.client.auth.data.AuthRepository
import com.anchorguard.client.auth.security.PinSecurityManager
import com.anchorguard.client.auth.security.SecureSessionStore
import com.anchorguard.client.ui.components.AnchorPrimaryButton
import com.anchorguard.client.ui.components.AnchorSecondaryButton
import com.anchorguard.client.ui.components.ExpandableHelpCard
import com.anchorguard.client.ui.components.WizardProgressIndicator
import com.anchorguard.client.ui.screens.ScreenScaffold

@Composable
fun WizardCompleteScreen(
    onFinish: () -> Unit,
    onReview: () -> Unit,
    onBack: () -> Unit,
    currentStep: Int = 6,
    totalSteps: Int = 6
) {
    val context = LocalContext.current
    val repo = remember { AuthRepository(context) }
    val store = remember { SecureSessionStore(context) }
    val pinManager = remember { PinSecurityManager(context) }

    val deviceRegistered = repo.deviceId()?.isNotBlank() == true
    val permissionsGranted = store.allWizardPermissionsGranted()
    val paired = store.isDevicePaired()
    val pinSet = pinManager.isPinSet()
    val sosEnabled = store.isSosEnabled()
    val geofenceEnabled = store.isGeofenceEnabled()
    val role = store.wizardRole()

    val hasPendingItems = !deviceRegistered || !permissionsGranted || !paired
    val canFinish = deviceRegistered

    ScreenScaffold(
        title = "Setup complete",
        subtitle = "Review your configuration before finishing."
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = androidx.compose.ui.Alignment.Start
        ) {
            WizardProgressIndicator(currentStep = currentStep, totalSteps = totalSteps)

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

            Spacer(Modifier.height(16.dp))

            // Finish-with-pending-items warning banner
            if (hasPendingItems) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        androidx.compose.foundation.layout.Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "\u26A0",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.width(20.dp).height(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Some items are not yet configured. Enforcement will not work until they are set up.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "You can finish now and complete the remaining items from the Parent Dashboard later.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
            }

            // Expandable help card for finish-with-pending
            ExpandableHelpCard(
                title = "What if I finish with pending items?",
                initiallyExpanded = false,
                content = {
                    Column {
                        Text(
                            text = "Finishing setup saves your progress and routes you to the dashboard. If enforcement permissions or pairing are pending, AnchorGuard cannot block apps or enforce schedules until those are completed. You can return to Settings at any time to finish setup.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            )

            Spacer(Modifier.height(24.dp))

            AnchorPrimaryButton(
                text = "Finish",
                onClick = {
                    store.setSetupWizardCompleted(true)
                    store.setWizardRole(role ?: "child")
                    onFinish()
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = canFinish && !store.setupWizardCompleted()
            )
            Spacer(Modifier.height(8.dp))
            RowSummaryActions(onReview, onBack)

            // Shared device mode indicator
            if (store.isBothRole()) {
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "Shared Device Mode",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Current mode: ${store.activeMode()?.capitalize() ?: "parent"}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Switch modes anytime from the Parent Dashboard using your Parent PIN.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
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
