package com.anchorguard.client.ui.screens

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.anchorguard.client.BuildConfig
import com.anchorguard.client.auth.data.AuthRepository
import com.anchorguard.client.auth.security.SecureSessionStore
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(
    context: Context,
    onFinished: (() -> Unit)? = null,
    onDeviceReady: (() -> Unit)? = null,
    onNeedsPairing: (() -> Unit)? = null,
    onNeedsRegistration: (() -> Unit)? = null,
    onWizardNeeded: (() -> Unit)? = null
) {
    val repo = remember { AuthRepository(context) }
    val store = remember { SecureSessionStore(context) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        // Brief splash display for brand visibility
        delay(800)

        // Check if setup wizard has been completed
        if (!store.setupWizardCompleted()) {
            onWizardNeeded?.invoke()
            return@LaunchedEffect
        }

        val isPaired = repo.isDevicePaired()
        val hasDeviceId = repo.deviceId()?.isNotBlank() == true

        when {
            isPaired -> {
                // Device is registered and paired — proceed directly to child home
                onDeviceReady?.invoke()
            }
            hasDeviceId -> {
                // Device is registered but not paired — show pairing screen
                onNeedsPairing?.invoke()
            }
            else -> {
                // Not registered — auto-register in background
                scope.launch {
                    try {
                        repo.registerDevice()
                        onNeedsPairing?.invoke()
                    } catch (_: Exception) {
                        // Registration failed — still show pairing screen so user can retry
                        onNeedsPairing?.invoke()
                    }
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "AnchorGuard", style = MaterialTheme.typography.headlineLarge)
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Family device safety, on your terms",
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = "Version ${BuildConfig.VERSION_NAME}",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
