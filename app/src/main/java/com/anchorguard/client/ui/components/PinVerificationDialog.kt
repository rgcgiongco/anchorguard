package com.anchorguard.client.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.anchorguard.client.auth.security.PinSecurityManager
import com.anchorguard.client.auth.security.PinVerifyResult

/**
 * PIN verification dialog for accessing child settings or parent mode.
 * Shows lockout state and max attempts.
 */
@Composable
fun PinVerificationDialog(
    onVerified: () -> Unit,
    onDismiss: () -> Unit,
    title: String = "Enter Parent PIN",
    subtitle: String = "PIN required to access settings"
) {
    val context = LocalContext.current
    val pinManager = remember { PinSecurityManager(context) }
    var pin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var isVerifying by remember { mutableStateOf(false) }
    var lockoutSeconds by remember { mutableStateOf(0L) }

    val lockoutState = pinManager.lockoutState()
    if (lockoutState.isLocked()) {
        val remaining = lockoutState.remainingSeconds()
        if (remaining > 0) {
            lockoutSeconds = remaining
        }
    }

    val currentError = error

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text(title, style = MaterialTheme.typography.titleLarge) },
        text = {
            Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(12.dp))
                if (lockoutSeconds > 0) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = "Locked",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Too many attempts. Try again in ${lockoutSeconds}s",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                } else if (currentError != null) {
                    Text(
                        text = currentError,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
                androidx.compose.material3.OutlinedTextField(
                    value = pin,
                    onValueChange = { newPin ->
                        pin = newPin.take(8).filter { it.isDigit() }
                    },
                    label = { Text("PIN") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = lockoutSeconds <= 0
                )
            }
        },
        confirmButton = {
            if (lockoutSeconds > 0) {
                TextButton(onClick = onDismiss) {
                    Text(text = "OK", style = MaterialTheme.typography.labelLarge)
                }
            } else {
                Button(
                    onClick = {
                        if (pin.isBlank()) {
                            error = "Please enter your PIN"
                            return@Button
                        }
                        isVerifying = true
                        val result = pinManager.verifyPin(pin)
                        isVerifying = false
                        when (result) {
                            is PinVerifyResult.Success -> {
                                pinManager.setParentSessionValid()
                                onVerified()
                            }
                            is PinVerifyResult.Failure -> {
                                val state = result.state
                                error = "Incorrect PIN (${state.failedAttempts}/${PinSecurityManager.MAX_FAILED_ATTEMPTS})"
                                pin = ""
                            }
                            is PinVerifyResult.Locked -> {
                                val state = result.state
                                lockoutSeconds = state.remainingSeconds()
                                error = "Too many attempts. Try again in ${lockoutSeconds}s"
                                pin = ""
                            }
                            PinVerifyResult.NotSet -> {
                                error = "No PIN set. Please set up a PIN first."
                            }
                            PinVerifyResult.InvalidFormat -> {
                                error = "Invalid PIN format"
                            }
                        }
                    },
                    enabled = pin.length >= 4 && lockoutSeconds <= 0 && !isVerifying,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    if (isVerifying) {
                        CircularProgressIndicator(
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(20.dp, 20.dp)
                        )
                    } else {
                        Text(text = "Verify")
                    }
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Cancel")
            }
        }
    )
}
