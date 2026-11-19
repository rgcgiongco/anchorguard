package com.anchorguard.client.ui.screens.wizard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.anchorguard.client.auth.data.AuthRepository
import com.anchorguard.client.network.NetworkResult
import com.anchorguard.client.ui.components.AnchorPrimaryButton
import com.anchorguard.client.ui.components.AnchorSecondaryButton
import com.anchorguard.client.ui.components.ExpandableHelpCard
import com.anchorguard.client.ui.components.WizardProgressIndicator
import com.anchorguard.client.ui.screens.ScreenScaffold
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

@Composable
fun WizardPairingScreen(
    onPaired: () -> Unit,
    onSkipped: () -> Unit,
    onBack: () -> Unit,
    currentStep: Int = 4,
    totalSteps: Int = 6
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repo = remember { AuthRepository(context) }
    var code by rememberSaveable { mutableStateOf("") }
    var inputCode by rememberSaveable { mutableStateOf("") }
    var expiresAt by rememberSaveable { mutableStateOf("") }
    var error by rememberSaveable { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }
    var parentSession by remember { mutableStateOf(repo.hasParentSession()) }
    var secondsRemaining by remember { mutableStateOf(600) }

    // Countdown timer: refresh when code changes, tick down every second
    LaunchedEffect(code) {
        secondsRemaining = 600
        while (code.isNotBlank() && secondsRemaining > 0) {
            delay(1000)
            secondsRemaining -= 1
        }
    }

    ScreenScaffold(
        title = "Pair this device",
        subtitle = "Pair this child device with your parent account."
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = androidx.compose.ui.Alignment.Start
        ) {
            WizardProgressIndicator(currentStep = currentStep, totalSteps = totalSteps)

            ExpandableHelpCard(
                title = "What is pairing?",
                initiallyExpanded = false
            ) {
                Column {
                    Text(
                        text = "Pairing links this child device to your parent account so you can manage it from the Parent Dashboard. A 6-character code is generated below — enter it on your parent phone or web console, or enter it here if you're already signed in as a parent.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            CardPairingCode(code, expiresAt)

            Spacer(Modifier.height(16.dp))

            if (code.isNotBlank()) {
                Text(
                    text = if (secondsRemaining > 0) "Code expires in ${formatTime(secondsRemaining)}" else "Code expired",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (secondsRemaining > 0) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.error
                )
            }

            AnchorPrimaryButton(
                text = if (busy) "Refreshing…" else "Refresh Code",
                onClick = {
                    if (busy) return@AnchorPrimaryButton
                    error = null
                    busy = true
                    secondsRemaining = 600
                    scope.launch {
                        when (val r = repo.refreshPairingCode()) {
                            is NetworkResult.Success -> {
                                code = r.data.pairingCode
                                expiresAt = r.data.pairingExpiresAt
                            }
                            is NetworkResult.Failure -> error = r.error.userMessage()
                        }
                        busy = false
                    }
                },
                enabled = !busy
            )

            Spacer(Modifier.height(24.dp))

            if (parentSession) {
                Text(
                    text = "Complete pairing on this device",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = inputCode,
                    onValueChange = { inputCode = it },
                    label = { Text("Pairing code (6 chars)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                AnchorPrimaryButton(
                    text = if (busy) "Pairing…" else "Pair device",
                    onClick = {
                        if (busy || inputCode.length < 6) return@AnchorPrimaryButton
                        error = null
                        busy = true
                        scope.launch {
                            when (val r = repo.pairDevice(inputCode)) {
                                is NetworkResult.Success -> onPaired()
                                is NetworkResult.Failure -> error = r.error.userMessage()
                            }
                            busy = false
                        }
                    },
                    enabled = !busy && inputCode.length == 6
                )
                Spacer(Modifier.height(16.dp))
            } else {
                Text(
                    text = "Or pair from another device",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Sign in as parent on your phone or web console and enter this code to pair.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (error != null) {
                Spacer(Modifier.height(12.dp))
                Text(error!!, color = MaterialTheme.colorScheme.error)
            }

            Spacer(Modifier.height(20.dp))
            AnchorPrimaryButton(
                text = "Skip for now",
                onClick = onSkipped,
                enabled = !busy
            )
            Spacer(Modifier.height(8.dp))
            AnchorSecondaryButton(text = "Back", onClick = onBack)
        }
    }
}

@Composable
private fun CardPairingCode(code: String, expiresAt: String) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .align(Alignment.CenterHorizontally),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Your pairing code",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = code.takeIf { it.isNotBlank() } ?: "—",
                style = MaterialTheme.typography.displaySmall
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (expiresAt.isNotBlank()) "Expires: $expiresAt" else "Code expires soon",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun formatTime(seconds: Int): String {
    val minutes = TimeUnit.SECONDS.toMinutes(seconds.toLong())
    val secs = seconds - minutes.toInt() * 60
    return "${minutes.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}"
}
