package com.anchorguard.client.ui.screens

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.anchorguard.client.auth.security.PinSecurityManager
import com.anchorguard.client.ui.components.AnchorPrimaryButton

@Composable
fun PinCreateScreen(
    onContinue: () -> Unit,
    onSkipIfSet: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val pinManager = remember { PinSecurityManager(context) }
    var pin by rememberSaveable { mutableStateOf("") }
    var confirm by rememberSaveable { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    if (pinManager.isPinSet() && onSkipIfSet != null) {
        androidx.compose.runtime.LaunchedEffect(Unit) { onSkipIfSet() }
    }

    val canContinue = pin.length >= 4 && pin == confirm

    ScreenScaffold(
        title = "Create parent PIN",
        subtitle = "Set a 4–8 digit PIN to protect parent controls. Entry is masked and stored securely on this device."
    ) {
        OutlinedTextField(
            value = pin,
            onValueChange = { if (it.length <= 8 && it.all(Char::isDigit)) pin = it },
            label = { Text("PIN") },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = confirm,
            onValueChange = { if (it.length <= 8 && it.all(Char::isDigit)) confirm = it },
            label = { Text("Confirm PIN") },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true
        )
        if (error != null) {
            Spacer(Modifier.height(8.dp))
            Text(error!!, color = MaterialTheme.colorScheme.error)
        }
        Spacer(Modifier.height(20.dp))
        AnchorPrimaryButton(
            text = "Save PIN",
            onClick = {
                if (pin != confirm) {
                    error = "PINs do not match."
                    return@AnchorPrimaryButton
                }
                val ok = pinManager.createPin(pin)
                if (ok) {
                    error = null
                    onContinue()
                } else {
                    error = "PIN must be 4–8 digits."
                }
            },
            enabled = canContinue
        )
    }
}
