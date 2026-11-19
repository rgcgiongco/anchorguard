package com.anchorguard.client.ui.screens

import android.content.Context
import android.os.Build
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.anchorguard.client.auth.data.AuthRepository
import com.anchorguard.client.network.ConnectivityState
import com.anchorguard.client.network.NetworkResult
import com.anchorguard.client.ui.components.AnchorInfoCard
import com.anchorguard.client.ui.components.AnchorPrimaryButton
import com.anchorguard.client.ui.components.AnchorSecondaryButton
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Instant

@Composable
fun PairingScreen(
    onPaired: () -> Unit,
    onNeedSignIn: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repo = remember { AuthRepository(context) }
    var code by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }
    var registeredCode by remember { mutableStateOf<String?>(null) }
    var registeredExpiresAtMs by remember { mutableLongStateOf(0L) }
    var nowMs by remember { mutableLongStateOf(System.currentTimeMillis()) }
    val online by ConnectivityState.online.collectAsState()

    LaunchedEffect(Unit) {
        while (true) {
            nowMs = System.currentTimeMillis()
            delay(1000)
        }
    }

    // Auto-load existing pairing code if device already registered
    LaunchedEffect(Unit) {
        val deviceId = repo.deviceId()
        if (deviceId != null && deviceId.isNotBlank() && registeredCode.isNullOrBlank()) {
            // Device is registered; check if we have a stored pairing code or need to refresh
            val apiKey = repo.secureSession().deviceApiKey()
            if (apiKey != null && apiKey.isNotBlank()) {
                registeredCode = apiKey
                registeredExpiresAtMs = Long.MAX_VALUE // Treat as valid until refreshed
            }
        }
    }

    val hasParentSession = repo.hasParentSession()
    val isDevicePaired = repo.isDevicePaired()

    ScreenScaffold(
        title = if (isDevicePaired) "Device Linked" else "Pair device",
        subtitle = if (hasParentSession)
            "Enter the 6-character pairing code to link this device to a parent account."
        else
            "Register this child device to get a pairing code, then pair it from a signed-in parent session."
    ) {
        if (!online) {
            AnchorInfoCard(
                title = "Offline",
                body = "You're offline. Pairing and registration require a connection."
            )
            Spacer(Modifier.height(12.dp))
        }

        // Show existing pairing code if device is registered
        if (!registeredCode.isNullOrBlank() && !isDevicePaired) {
            Spacer(Modifier.height(12.dp))
            val remaining = ((registeredExpiresAtMs - nowMs).coerceAtLeast(0L) / 1000L)
            val codeExpired = registeredExpiresAtMs != Long.MAX_VALUE && remaining <= 0
            AnchorInfoCard(
                title = if (codeExpired) "Pairing code expired" else "Pairing code",
                body = if (codeExpired)
                    "Your pairing code has expired. Tap Refresh to generate a new code."
                else
                    "Code: ${registeredCode}\nExpires in: ${if (registeredExpiresAtMs == Long.MAX_VALUE) "valid" else "${remaining}s"}\nUse this code from a signed-in parent account to complete pairing."
            )
            Spacer(Modifier.height(12.dp))
            AnchorSecondaryButton(
                text = if (busy) "Refreshing…" else "Refresh code",
                onClick = {
                    if (busy) return@AnchorSecondaryButton
                    if (!ConnectivityState.isOnline(context)) {
                        error = "You're offline. Cannot refresh code."
                        return@AnchorSecondaryButton
                    }
                    error = null
                    busy = true
                    scope.launch {
                        when (val r = repo.refreshPairingCode()) {
                            is NetworkResult.Success -> {
                                registeredCode = r.data.pairingCode
                                registeredExpiresAtMs = parseExpiryMs(r.data.pairingExpiresAt)
                                error = null
                            }
                            is NetworkResult.Failure -> error = r.error.userMessage()
                        }
                        busy = false
                    }
                },
                enabled = !busy && online
            )
        } else if (!isDevicePaired && online) {
            // Device not registered yet - show registration message
            AnchorInfoCard(
                title = "Device registration",
                body = "Registering your device automatically on startup. Please wait…"
            )
            Spacer(Modifier.height(12.dp))
            AnchorSecondaryButton(
                text = if (busy) "Registering device…" else "Register this device",
                onClick = {
                    if (busy) return@AnchorSecondaryButton
                    if (!ConnectivityState.isOnline(context)) {
                        error = "You're offline. Changes will sync when reconnected."
                        return@AnchorSecondaryButton
                    }
                    error = null
                    busy = true
                    scope.launch {
                        when (val r = repo.registerDevice()) {
                            is NetworkResult.Success -> {
                                registeredCode = r.data.pairingCode
                                registeredExpiresAtMs = parseExpiryMs(r.data.pairingExpiresAt)
                                error = null
                            }
                            is NetworkResult.Failure -> error = r.error.userMessage()
                        }
                        busy = false
                    }
                },
                enabled = !busy && online
            )
        }

        // Complete pairing section (only when user has parent session)
        if (hasParentSession && !isDevicePaired) {
            Spacer(Modifier.height(16.dp))
            AnchorInfoCard(
                title = "Complete pairing",
                body = "Enter the pairing code from this device to bind it to your parent account."
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = code,
                onValueChange = { code = it.uppercase().filter { c -> c.isLetterOrDigit() }.take(6) },
                label = { Text("Pairing code (6 characters)") },
                singleLine = true,
                enabled = !busy
            )
            Spacer(Modifier.height(8.dp))
            if (error != null) {
                Text(error!!, color = MaterialTheme.colorScheme.error)
            }
            Spacer(Modifier.height(20.dp))
            AnchorPrimaryButton(
                text = if (busy) "Pairing…" else "Pair device",
                onClick = {
                    if (busy) return@AnchorPrimaryButton
                    if (!ConnectivityState.isOnline(context)) {
                        error = "You're offline. Changes will sync when reconnected."
                        return@AnchorPrimaryButton
                    }
                    error = null
                    busy = true
                    scope.launch {
                        when (val r = repo.pairDevice(code)) {
                            is NetworkResult.Success -> onPaired()
                            is NetworkResult.Failure -> error = r.error.userMessage()
                        }
                        busy = false
                    }
                },
                enabled = hasParentSession && code.length == 6 && !busy && online
            )
        } else if (!hasParentSession && !isDevicePaired && onNeedSignIn != null) {
            Spacer(Modifier.height(16.dp))
            AnchorSecondaryButton(text = "Sign in as parent to pair", onClick = onNeedSignIn)
        }

        if (isDevicePaired) {
            Spacer(Modifier.height(16.dp))
            AnchorInfoCard(
                title = "Device linked",
                body = "This device is paired and protected. Use the parent dashboard to manage settings."
            )
            Spacer(Modifier.height(12.dp))
            AnchorPrimaryButton(
                text = "Go to dashboard",
                onClick = onPaired
            )
        }
    }
}

private fun parseExpiryMs(value: String): Long {
    return try {
        if (Build.VERSION.SDK_INT >= 26) Instant.parse(value).toEpochMilli() else 0L
    } catch (_: Throwable) {
        0L
    }
}
