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
import androidx.fragment.app.FragmentActivity
import com.anchorguard.client.auth.security.PinSecurityManager
import com.anchorguard.client.auth.security.PinVerifyResult
import com.anchorguard.client.auth.ui.BiometricGate
import com.anchorguard.client.enforcement.EnforcementController
import com.anchorguard.client.ui.components.AnchorPrimaryButton
import com.anchorguard.client.ui.components.AnchorSecondaryButton
import kotlinx.coroutines.delay

@Composable
fun ParentGateScreen(
    onUnlocked: () -> Unit,
    onForgotPin: () -> Unit,
    onChangePin: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val activity = context as? FragmentActivity
    val pinManager = remember { PinSecurityManager(context) }
    var pin by rememberSaveable { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var lockoutSeconds by remember { mutableStateOf(0L) }
    val biometricAvailable = remember { BiometricGate.canAuthenticate(context) }

    // If session still valid, allow quick unlock
    LaunchedEffect(Unit) {
        if (pinManager.isParentSessionValid()) {
            EnforcementController.enterParentMode(context)
            onUnlocked()
            return@LaunchedEffect
        }
        if (biometricAvailable && activity != null) {
            BiometricGate.authenticate(
                activity = activity,
                title = "Parent access",
                subtitle = "Use biometrics to access parent controls",
                onSuccess = {
                    EnforcementController.enterParentMode(context)
                    onUnlocked()
                },
                onFailure = { msg -> error = msg ?: "Biometric authentication failed." },
                onCancel = { /* fall back to PIN */ }
            )
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            val state = pinManager.lockoutState()
            lockoutSeconds = if (state.isLocked()) state.remainingSeconds() else 0L
            delay(1000)
        }
    }

    val locked = lockoutSeconds > 0

    ScreenScaffold(
        title = "Parent access",
        subtitle = "Enter your PIN to continue. Biometric fallback is available when enabled on this device."
    ) {
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
                        error = null
                        pin = ""
                        EnforcementController.enterParentMode(context)
                        onUnlocked()
                    }
                    is PinVerifyResult.Locked -> {
                        lockoutSeconds = r.state.remainingSeconds()
                        error = null
                    }
                    is PinVerifyResult.Failure -> {
                        pin = ""
                        lockoutSeconds = if (r.state.isLocked()) r.state.remainingSeconds() else 0L
                        val attempts = r.state.failedAttempts
                        error = "Incorrect PIN${if (attempts > 0) " (${attempts}/${PinSecurityManager.MAX_FAILED_ATTEMPTS})" else ""}."
                    }
                    is PinVerifyResult.NotSet -> error = "No PIN is set. Create a PIN first."
                    is PinVerifyResult.InvalidFormat -> error = "Enter a valid PIN."
                }
            },
            enabled = !locked && pin.length >= 4
        )
        if (biometricAvailable && activity != null) {
            Spacer(Modifier.height(8.dp))
            AnchorSecondaryButton(
                text = "Use biometrics",
                onClick = {
                    BiometricGate.authenticate(
                        activity = activity,
                        title = "Parent access",
                        subtitle = "Use biometrics to access parent controls",
                        onSuccess = {
                            EnforcementController.enterParentMode(context)
                            onUnlocked()
                        },
                        onFailure = { msg -> error = msg ?: "Biometric authentication failed." },
                        onCancel = { }
                    )
                },
                enabled = !locked
            )
        }
        if (onChangePin != null) {
            Spacer(Modifier.height(8.dp))
            TextButton(onClick = onChangePin) { Text("Change PIN") }
        }
        Spacer(Modifier.height(4.dp))
        TextButton(onClick = onForgotPin) { Text("Forgot PIN") }
    }
}
