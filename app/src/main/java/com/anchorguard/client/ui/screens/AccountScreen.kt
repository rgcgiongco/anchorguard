package com.anchorguard.client.ui.screens

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.anchorguard.client.R
import com.anchorguard.client.auth.data.AuthRepository
import com.anchorguard.client.auth.security.PinSecurityManager
import com.anchorguard.client.auth.security.SecureSessionStore
import com.anchorguard.client.data.local.AnchorGuardDatabase
import com.anchorguard.client.data.local.UserRepository
import com.anchorguard.client.enforcement.EnforcementController
import com.anchorguard.client.model.SubscriptionTier
import com.anchorguard.client.network.ConnectivityState
import com.anchorguard.client.ui.components.AnchorInfoCard
import com.anchorguard.client.ui.components.AnchorSecondaryButton
import com.anchorguard.client.ui.components.AnchorSectionDivider
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AccountScreen(
    onChangePin: (() -> Unit)? = null,
    onSignOut: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val db = remember(context) { AnchorGuardDatabase.getInstance(context) }
    val authRepo = remember { AuthRepository(context) }
    val session = remember { SecureSessionStore(context) }
    val userRepo = remember { UserRepository(context) }
    val userTier by remember { mutableStateOf(userRepo.getUserTier()) }
    val pinManager = remember { PinSecurityManager(context) }
    val statuses by db.syncStatusDao().getAll().collectAsState(initial = emptyList())
    val online by ConnectivityState.online.collectAsState()
    var signingOut by remember { mutableStateOf(false) }

    val syncBody = if (statuses.isEmpty()) {
        "Cloud sync not yet recorded. Usage events are stored locally and will upload when device sync is configured and network is available."
    } else {
        statuses.joinToString("\n") { s ->
            val ts = s.lastSyncedAt?.let { formatSyncTime(it) } ?: "never"
            "${s.tableName}: last sync $ts, pending ${s.pendingCount}"
        }
    }

    val accountBody = buildString {
        append("Email: ${session.parentEmail() ?: "Not signed in"}\n")
        append("Parent session: ${if (authRepo.hasParentSession()) "Active token stored" else "No active session"}\n")
        append("Device paired: ${if (authRepo.isDevicePaired()) "Yes" else "No/unknown"}\n")
        append("PIN configured: ${if (pinManager.isPinSet()) "Yes" else "No"}\n")
        append("Network: ${if (online) "Online" else "Offline"}")
    }

    ScreenScaffold(
        title = "Account",
        subtitle = "Parent account, security, and sync status."
    ) {
        if (!online) {
            AnchorInfoCard(
                title = "Offline",
                body = "You're offline. Usage and policy changes will sync when reconnected."
            )
            Spacer(Modifier.height(12.dp))
        }
        AnchorInfoCard(title = "Parent account", body = accountBody)
        Spacer(Modifier.height(12.dp))
        AnchorInfoCard(title = "Sync status", body = syncBody)
        Spacer(Modifier.height(12.dp))
        if (onChangePin != null && pinManager.isPinSet()) {
            AnchorSecondaryButton(text = "Change PIN", onClick = onChangePin)
            Spacer(Modifier.height(8.dp))
        }
        AnchorSecondaryButton(
            text = if (signingOut) "Signing out…" else "Sign out",
            onClick = {
                if (signingOut) return@AnchorSecondaryButton
                signingOut = true
                scope.launch {
                    try {
                        authRepo.logout()
                    } catch (_: Throwable) {
                        session.setParentTokens(null, null)
                    }
                    pinManager.clearParentSession()
                    EnforcementController.exitParentMode(context)
                    signingOut = false
                    onSignOut?.invoke()
                }
            },
            enabled = !signingOut
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Sign out clears parent session tokens. PIN and device pairing remain on this device unless reset separately.",
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(Modifier.height(16.dp))
        AnchorSectionDivider()
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Support",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(8.dp))
        AnchorSecondaryButton(text = "Community Forum", onClick = {})
        Spacer(Modifier.height(8.dp))
        if (userTier.tier == SubscriptionTier.PAID) {
            AnchorSecondaryButton(text = "Email Support", onClick = {})
            Spacer(Modifier.height(8.dp))
        }
    }
}

private fun formatSyncTime(epochMs: Long): String {
    return try {
        SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(epochMs))
    } catch (_: Throwable) {
        epochMs.toString()
    }
}
