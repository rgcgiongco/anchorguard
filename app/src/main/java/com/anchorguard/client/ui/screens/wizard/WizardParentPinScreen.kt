package com.anchorguard.client.ui.screens.wizard

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.anchorguard.client.auth.security.PinSecurityManager
import com.anchorguard.client.ui.screens.ScreenScaffold
import com.anchorguard.client.ui.components.AnchorPrimaryButton
import com.anchorguard.client.ui.components.AnchorSecondaryButton
import kotlinx.coroutines.launch

/**
 * Mandatory parent PIN setup screen.
 * Requires a 4-6 digit PIN before proceeding to the parent dashboard.
 */
@Composable
fun WizardParentPinScreen(
    onPinSet: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val pinManager = remember { PinSecurityManager(context) }
    var pin by rememberSaveable { mutableStateOf("") }
    var confirmPin by rememberSaveable { mutableStateOf("") }
    var error by rememberSaveable { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }

    val isValidPin = pin.length >= 4 && pin.length <= 8 && pin.all { it.isDigit() }
    val pinsMatch = pin == confirmPin && pin.isNotEmpty()

    ScreenScaffold(
        title = "Set parent PIN",
        subtitle = "Create a 4–6 digit PIN to protect parent settings."
    ) {
        OutlinedTextField(
            value = pin,
            onValueChange = { pin = it },
            label = { Text(text = "PIN") },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = confirmPin,
            onValueChange = { confirmPin = it },
            label = { Text(text = "Confirm PIN") },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true
        )
        if (error != null) {
            Spacer(Modifier.height(8.dp))
            Text(error!!, color = MaterialTheme.colorScheme.error)
        } else if (!isValidPin && pin.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            Text("PIN must be 4–6 digits.", color = MaterialTheme.colorScheme.error)
        } else if (!pinsMatch && confirmPin.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            Text("PINs do not match.", color = MaterialTheme.colorScheme.error)
        }
        Spacer(Modifier.height(20.dp))
        AnchorPrimaryButton(
            text = if (busy) "Setting PIN…" else "Set PIN",
            onClick = {
                if (busy) return@AnchorPrimaryButton
                if (!isValidPin) { error = "PIN must be 4–6 digits."; return@AnchorPrimaryButton }
                if (!pinsMatch) { error = "PINs do not match."; return@AnchorPrimaryButton }
                error = null
                busy = true
                scope.launch {
                    val success = pinManager.createPin(pin)
                    busy = false
                    if (success) {
                        pinManager.setSetupComplete(true)
                        onPinSet()
                    } else {
                        error = "Failed to set PIN. Please try again."
                    }
                }
            },
            enabled = !busy && isValidPin && pinsMatch
        )
        Spacer(Modifier.height(8.dp))
        TextButton(onClick = onBack) { Text("Back") }
    }
}
