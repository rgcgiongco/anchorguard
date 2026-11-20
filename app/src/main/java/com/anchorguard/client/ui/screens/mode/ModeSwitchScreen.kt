package com.anchorguard.client.ui.screens.mode

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.anchorguard.client.auth.security.PinSecurityManager
import com.anchorguard.client.auth.security.PinVerifyResult
import com.anchorguard.client.auth.security.SecureSessionStore
import com.anchorguard.client.ui.components.AnchorChildModeBanner
import com.anchorguard.client.ui.components.AnchorModeSwitch
import com.anchorguard.client.ui.components.AnchorParentPinDialog
import com.anchorguard.client.ui.components.AnchorPrimaryButton
import com.anchorguard.client.ui.screens.ScreenScaffold

@Composable
fun ModeSwitchScreen(
    onBack: () -> Unit,
    onModeChanged: (String) -> Unit
) {
    val context = LocalContext.current
    val store = remember { SecureSessionStore(context) }
    val pinManager = remember { PinSecurityManager(context) }

    val role = store.deviceRole()
    val currentMode = store.activeMode() ?: "parent"
    val isBoth = role == "both"

    var showPinDialog by remember { mutableStateOf(false) }
    var pinError by remember { mutableStateOf<String?>(null) }
    var switchingTo by remember { mutableStateOf<String?>(null) }

    // canSwitchToChild removed - using currentMode directly

    ScreenScaffold(
        title = "Device Mode",
        subtitle = when {
            isBoth -> "Switch between Parent and Child access"
            else -> "This device is set up as ${role?.capitalize() ?: "unknown"}."
        },
    ) {
        if (!isBoth) {
            Text(
                text = "Shared device mode is not enabled for this device.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            Spacer(modifier = Modifier.height(24.dp))

            AnchorModeSwitch(
                currentMode = currentMode,
                onModeSwitchRequested = {
                    switchingTo = if (currentMode == "parent") "child" else "parent"
                    showPinDialog = true
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = true
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "How it works",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = buildString {
                    append(when (currentMode) {
                        "parent" -> "Parent Mode: Full access to all apps and settings. AnchorGuard runs in the background.\n\n"
                        else -> "Child Mode: App restrictions and schedules are enforced. Blocked apps cannot be opened.\n\n"
                    })
                    append("Switch to ${if (currentMode == "parent") "Child" else "Parent"} Mode for ${if (currentMode == "parent") "restricted access" else "unrestricted access"}.")
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (currentMode == "child") {
                AnchorChildModeBanner(
                    onSwitchToParent = {
                        switchingTo = "parent"
                        showPinDialog = true
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            val targetMode = if (currentMode == "parent") "child" else "parent"
            AnchorPrimaryButton(
                text = "Switch to ${targetMode.capitalize()} Mode",
                onClick = {
                    switchingTo = targetMode
                    showPinDialog = true
                },
                modifier = Modifier.fillMaxWidth()
            )
        }

        AnchorParentPinDialog(
            open = showPinDialog,
            onDismiss = {
                showPinDialog = false
                pinError = null
                switchingTo = null
            },
            onPinEntered = { pin ->
                switchingTo?.let { target ->
                    val result = pinManager.verifyPin(pin)
                    when (result) {
                        is PinVerifyResult.Success -> {
                            store.setActiveMode(target)
                            pinManager.setParentSessionValid()
                            onModeChanged(target)
                            showPinDialog = false
                            pinError = null
                            switchingTo = null
                        }
                        is PinVerifyResult.Failure -> {
                            pinError = "Incorrect PIN. Please try again."
                        }
                        is PinVerifyResult.InvalidFormat -> {
                            pinError = "Invalid PIN format."
                        }
                        is PinVerifyResult.Locked -> {
                            pinError = "Too many attempts. Try again in ${result.state.remainingSeconds()} seconds."
                        }
                        PinVerifyResult.NotSet -> {
                            pinError = "No Parent PIN is set. Create one in Settings first."
                            showPinDialog = false
                            switchingTo = null
                        }
                    }
                    showPinDialog = false
                    switchingTo = null
                }
            },
            errorMessage = pinError
        )
    }
}

private fun String.capitalize(): String = if (isEmpty()) this else this[0].uppercase() + substring(1)
