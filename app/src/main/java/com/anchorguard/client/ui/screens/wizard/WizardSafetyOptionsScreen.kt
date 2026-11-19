package com.anchorguard.client.ui.screens.wizard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.anchorguard.client.auth.data.DevicePolicyRepository
import com.anchorguard.client.auth.security.PinSecurityManager
import com.anchorguard.client.auth.security.SecureSessionStore
import com.anchorguard.client.network.NetworkResult
import com.anchorguard.client.ui.components.AnchorPrimaryButton
import com.anchorguard.client.ui.components.AnchorSecondaryButton
import com.anchorguard.client.ui.components.AnchorSwitch
import com.anchorguard.client.ui.screens.ScreenScaffold
import kotlinx.coroutines.launch

@Composable
fun WizardSafetyOptionsScreen(
    onComplete: () -> Unit,
    onSkipped: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val pinManager = remember { PinSecurityManager(context) }
    val policyRepo = remember { DevicePolicyRepository(context) }
    val store = remember { SecureSessionStore(context) }
    val deviceId = policyRepo.deviceId()

    var pinEnabled by remember { mutableStateOf(false) }
    var sosEnabled by remember { mutableStateOf(store.isSosEnabled()) }
    var geofenceEnabled by remember { mutableStateOf(store.isGeofenceEnabled()) }

    var pin by rememberSaveable { mutableStateOf("") }
    var confirm by rememberSaveable { mutableStateOf("") }
    var pinError by remember { mutableStateOf<String?>(null) }
    var sosError by remember { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }

    ScreenScaffold(
        title = "Safety options",
        subtitle = "Set up optional protections. You can configure these later from the Parent Dashboard."
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = androidx.compose.ui.Alignment.Start
        ) {
            // Parent PIN section
            AnchorSwitch(
                checked = pinEnabled,
                onCheckedChange = { pinEnabled = it; if (!it) { pinError = null; pin = ""; confirm = "" } },
                label = "Parent PIN",
                description = "Create a 4–6 digit PIN for Parent Gate access on this device."
            )
            if (pinEnabled) {
                OutlinedTextField(
                    value = pin,
                    onValueChange = { if (it.length <= 6 && it.all { it.isDigit() }) pin = it },
                    label = { Text("PIN") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = confirm,
                    onValueChange = { if (it.length <= 6 && it.all { it.isDigit() }) confirm = it },
                    label = { Text("Confirm PIN") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                if (pinError != null) {
                    Spacer(Modifier.height(8.dp))
                    Text(pinError!!, color = MaterialTheme.colorScheme.error)
                }
            }

            Spacer(Modifier.height(12.dp))

            // SOS section
            AnchorSwitch(
                checked = sosEnabled,
                onCheckedChange = { it ->
                    sosEnabled = it
                    store.setSosEnabled(it)
                },
                label = "SOS Emergency",
                description = "Press and hold power button 3x, or use the SOS button, to alert your parent."
            )
            if (sosEnabled && sosError == null && !busy) {
                scope.launch {
                    busy = true
                    val result = when (val r = policyRepo.sendSos(deviceId ?: "", null, null, null)) {
                        is NetworkResult.Success -> {
                            sosEnabled = true
                            null
                        }
                        is NetworkResult.Failure -> {
                            sosError = r.error.userMessage()
                            sosEnabled = false
                            r.error.userMessage()
                        }
                    }
                    busy = false
                }
            }
            if (sosError != null) {
                Spacer(Modifier.height(8.dp))
                Text(sosError!!, color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(8.dp))
                AnchorSecondaryButton(text = "Retry SOS", onClick = { sosError = null; sosEnabled = true }, enabled = !busy)
            }

            Spacer(Modifier.height(12.dp))

            // Geofence section
            AnchorSwitch(
                checked = geofenceEnabled,
                onCheckedChange = { it ->
                    geofenceEnabled = it
                    store.setGeofenceEnabled(it)
                },
                label = "Safe Zones (Geofence)",
                description = "Set up home/school safe zones. Location-based alerts come from the Parent Dashboard."
            )
            if (geofenceEnabled) {
                Text(
                    text = "Geofence zones can be created from the Parent Dashboard after setup completes.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(24.dp))

            val pinValid = !pinEnabled || (pin.length >= 4 && pin.length <= 6 && pin == confirm)
            AnchorPrimaryButton(
                text = if (busy) "Saving…" else "Continue",
                onClick = {
                    if (busy || !pinValid) return@AnchorPrimaryButton
                    if (pinEnabled) {
                        pinError = null
                        if (pin != confirm) {
                            pinError = "PINs do not match."
                            return@AnchorPrimaryButton
                        }
                        val ok = pinManager.createPin(pin)
                        if (!ok) {
                            pinError = "PIN must be 4–6 digits."
                            return@AnchorPrimaryButton
                        }
                    }
                    onComplete()
                },
                enabled = !busy && pinValid
            )
            Spacer(Modifier.height(8.dp))
            AnchorSecondaryButton(text = "Skip all safety options", onClick = onSkipped)
            Spacer(Modifier.height(8.dp))
            AnchorSecondaryButton(text = "Back", onClick = onBack)
        }
    }
}
