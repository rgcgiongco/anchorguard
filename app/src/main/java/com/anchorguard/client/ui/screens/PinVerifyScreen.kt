package com.anchorguard.client.ui.screens

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import kotlinx.coroutines.delay

@Composable
fun PinVerifyScreen(
    title: String = "Enter PIN",
    subtitle: String = "Enter your parent PIN to continue.",
    onVerified: () -> Unit,
    onCancel: (() -> Unit)? = null,
    onUseBiometric: (() -> Unit)? = null,
    biometricAvailable: Boolean = false
) {
    val context = LocalContext.current
    val pinManager = remember { PinSecurityManager(context) }
    var pin by rememberSaveable { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var lockoutSeconds by remember { mutableStateOf(0L) }

    LaunchedEffect(Unit) {
        while (true) {
            val state = pinManager.lockoutState()
            lockoutSeconds = if (state.isLocked()) state.remainingSeconds() else 0L
            delay(1000)
        }
    }

    val locked = lockoutSeconds > 0

    ScreenScaffold(title = title, subtitle = subtitle) {
        OutlinedTextField(
            value = pin,
            onValueChange = { if (it.length <= 8 && it.all(Char::isDigit)) pin = it },
            label = { Text("PIN") },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            enabled = !locked
        )
        if (locked) {
            Spacer(Modifier.height(8.dp))
            Text("Too many attempts. Try again in ${lockoutSeconds}s.", color = MaterialTheme.colorScheme.error)
        }
        if (error != null && !locked) {
            Spacer(Modifier.height(8.dp))
            Text(error!!, color = MaterialTheme.colorScheme.error)
        }
        Spacer(Modifier.height(20.dp))
        AnchorPrimaryButton(
            text = "Unlock",
            onClick = {
                when (val r = pinManager.verifyPin(pin)) {
                    is PinVerifyResult.Success -> {
                        pinManager.setParentSessionValid()
                        pin = ""
                        error = null
                        onVerified()
                    }
                    is PinVerifyResult.Locked -> {
                        error = null
                        lockoutSeconds = r.state.remainingSeconds()
                    }
                    is PinVerifyResult.Failure -> {
                        pin = ""
                        error = "Incorrect PIN."
                        lockoutSeconds = if (r.state.isLocked()) r.state.remainingSeconds() else 0L
                    }
                    is PinVerifyResult.NotSet -> error = "PIN is not set."
                    is PinVerifyResult.InvalidFormat -> error = "Enter a valid PIN."
                }
            },
            enabled = !locked && pin.length >= 4
        )
        if (biometricAvailable && onUseBiometric != null) {
            Spacer(Modifier.height(8.dp))
            AnchorSecondaryButton(text = "Use biometrics", onClick = onUseBiometric, enabled = !locked)
        }
        if (onCancel != null) {
            Spacer(Modifier.height(8.dp))
            TextButton(onClick = onCancel) { Text("Cancel") }
        }
    }
}
