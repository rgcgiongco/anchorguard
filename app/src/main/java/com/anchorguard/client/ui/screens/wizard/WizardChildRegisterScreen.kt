package com.anchorguard.client.ui.screens.wizard

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.anchorguard.client.auth.data.AuthRepository
import com.anchorguard.client.auth.security.SecureSessionStore
import com.anchorguard.client.network.NetworkResult
import com.anchorguard.client.ui.components.AnchorPrimaryButton
import com.anchorguard.client.ui.screens.ScreenScaffold
import kotlinx.coroutines.launch

@Composable
fun WizardChildRegisterScreen(
    onRegistered: () -> Unit,
    onBack: () -> Unit,
    setupMode: String? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repo = remember { AuthRepository(context) }
    val store = remember { SecureSessionStore(context) }
    val mode = setupMode ?: store.setupMode() ?: "child_device"
    var busy by remember { mutableStateOf(false) }
    var error by rememberSaveable { mutableStateOf<String?>(null) }

    val isParentAddingChild = mode == "parent_adding_child"

    LaunchedEffect(Unit) {
        if (busy) return@LaunchedEffect
        busy = true
        error = null
        scope.launch {
            when (val r = repo.registerDevice()) {
                is NetworkResult.Success -> onRegistered()
                is NetworkResult.Failure -> {
                    error = r.error.userMessage()
                    busy = false
                }
            }
        }
    }

    ScreenScaffold(
        title = if (isParentAddingChild) "Registering child device" else "Registering device",
        subtitle = if (isParentAddingChild) {
            "This child device is being registered to your AnchorGuard account."
        } else {
            "This child device is being registered with your AnchorGuard account."
        }
    ) {
        if (busy) {
            Text(text = "Registering this device…", style = MaterialTheme.typography.bodyLarge)
        }
        if (error != null) {
            Spacer(Modifier.height(12.dp))
            Text(error!!, color = MaterialTheme.colorScheme.error)
            Spacer(Modifier.height(12.dp))
            AnchorPrimaryButton(
                text = "Retry",
                onClick = {
                    error = null
                    busy = true
                    scope.launch {
                        when (val r = repo.registerDevice()) {
                            is NetworkResult.Success -> onRegistered()
                            is NetworkResult.Failure -> {
                                error = r.error.userMessage()
                                busy = false
                            }
                        }
                    }
                },
                enabled = !busy
            )
        }
        Spacer(Modifier.height(8.dp))
        TextButton(onClick = onBack) { Text("Back") }
    }
}
