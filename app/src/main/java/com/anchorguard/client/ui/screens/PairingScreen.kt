package com.anchorguard.client.ui.screens

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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
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
    var code by rememberSaveable { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }
    var registeredCode by rememberSaveable { mutableStateOf<String?>(null) }
    var registeredExpiresAtMs by rememberSaveable { mutableLongStateOf(0L) }
    var nowMs by remember { mutableLongStateOf(System.currentTimeMillis()) }
    val online by ConnectivityState.online.collectAsStateWithLifecycleCompat()

    LaunchedEffect(Unit) {
        while (true) {
            nowMs = System.currentTimeMillis()
            delay(1000)
        }
    }

    val hasParentSession = repo.hasParentSession()

    ScreenScaffold(
        title = "Pair device",
        subtitle = if (hasParentSession)
            "Enter the 6-character pairing code to link this device to a parent account."
        else
            "Register this child device to get a pairing code, then pair it from a signed-in parent session."
    ) {
        if (!online) {
            AnchorInfoCard(
                title = "Offline",
                body = "You're offline. Pairing and registration require a connection; changes will sync when reconnected where applicable."
            )
            Spacer(Modifier.height(12.dp))
        }
        AnchorInfoCard(
            title = "Device registration",
            body = "On a child device, register to generate a pairing code and secure device key. Pairing codes are single-use and expire."
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

        if (!registeredCode.isNullOrBlank()) {
            Spacer(Modifier.height(12.dp))
            val remaining = ((registeredExpiresAtMs - nowMs).coerceAtLeast(0L) / 1000L)
            AnchorInfoCard(
                title = "Pairing code",
                body = "Code: ${registeredCode}\nExpires in: ${if (registeredExpiresAtMs > 0) "${remaining}s" else "unknown"}\nUse this code from a signed-in parent account to complete pairing."
            )
        }

        Spacer(Modifier.height(16.dp))
        AnchorInfoCard(
            title = "Complete pairing",
            body = if (hasParentSession)
                "If you are signed in as a parent on this device, enter the pairing code below to bind the device."
            else
                "Sign in with a parent account on this device to submit a pairing code, or pair from the web console."
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = code,
            onValueChange = { code = it.uppercase().filter { c -> c.isLetterOrDigit() }.take(6) },
            label = { Text("Pairing code (6 characters)") },
            singleLine = true,
            enabled = hasParentSession
        )

        if (!hasParentSession && onNeedSignIn != null) {
            Spacer(Modifier.height(8.dp))
            AnchorSecondaryButton(text = "Sign in as parent to pair", onClick = onNeedSignIn)
        }

        if (error != null) {
            Spacer(Modifier.height(8.dp))
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
    }
}

private fun parseExpiryMs(value: String): Long {
    return try {
        if (Build.VERSION.SDK_INT >= 26) Instant.parse(value).toEpochMilli() else 0L
    } catch (_: Throwable) {
        0L
    }
}

@Composable
private fun kotlinx.coroutines.flow.StateFlow<Boolean>.collectAsStateWithLifecycleCompat(): androidx.compose.runtime.State<Boolean> {
    val state = remember { androidx.compose.runtime.mutableStateOf(this.value) }
    LaunchedEffect(this) {
        this@collectAsStateWithLifecycleCompat.collect { state.value = it }
    }
    return state
}
