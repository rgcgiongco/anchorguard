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
import com.anchorguard.client.auth.security.PinVerifyResult
import com.anchorguard.client.ui.components.AnchorPrimaryButton
import com.anchorguard.client.ui.components.AnchorSecondaryButton

@Composable
fun PinChangeScreen(
    onChanged: () -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    val pinManager = remember { PinSecurityManager(context) }
    var oldPin by rememberSaveable { mutableStateOf("") }
    var newPin by rememberSaveable { mutableStateOf("") }
    var confirm by rememberSaveable { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    val canSubmit = oldPin.length >= 4 && newPin.length >= 4 && newPin == confirm

    ScreenScaffold(
        title = "Change PIN",
        subtitle = "Verify your current PIN, then choose a new one (4–8 digits)."
    ) {
        OutlinedTextField(
            value = oldPin,
            onValueChange = { if (it.length <= 8 && it.all(Char::isDigit)) oldPin = it },
            label = { Text("Current PIN") },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = newPin,
            onValueChange = { if (it.length <= 8 && it.all(Char::isDigit)) newPin = it },
            label = { Text("New PIN") },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = confirm,
            onValueChange = { if (it.length <= 8 && it.all(Char::isDigit)) confirm = it },
            label = { Text("Confirm new PIN") },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true
        )
        if (error != null) {
            Spacer(Modifier.height(8.dp))
            Text(error!!, color = MaterialTheme.colorScheme.error)
        }
        Spacer(Modifier.height(20.dp))
        AnchorPrimaryButton(
            text = "Save new PIN",
            onClick = {
                if (newPin != confirm) {
                    error = "New PINs do not match."
                    return@AnchorPrimaryButton
                }
                when (val r = pinManager.changePin(oldPin, newPin)) {
                    is PinVerifyResult.Success -> {
                        error = null
                        onChanged()
                    }
                    is PinVerifyResult.Locked -> error = "Too many attempts. Try again in ${r.state.remainingSeconds()}s."
                    is PinVerifyResult.Failure -> error = "Current PIN is incorrect."
                    is PinVerifyResult.NotSet -> error = "PIN is not set."
                    is PinVerifyResult.InvalidFormat -> error = "New PIN must be 4–8 digits."
                }
            },
            enabled = canSubmit
        )
        Spacer(Modifier.height(8.dp))
        AnchorSecondaryButton(text = "Cancel", onClick = onCancel)
    }
}
