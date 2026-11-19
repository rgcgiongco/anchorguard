package com.anchorguard.client.ui.screens

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import com.anchorguard.client.enforcement.EnforcementPermissionChecker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun SplashScreen(
    context: Context,
    onFinished: (() -> Unit)? = null,
    onDeviceReady: (() -> Unit)? = null,
    onNeedsPairing: (() -> Unit)? = null,
    onNeedsRegistration: (() -> Unit)? = null,
    onPermissionsNeeded: (() -> Unit)? = null,
    onWizardNeeded: (() -> Unit)? = null,
    onWizardComplete: (() -> Unit)? = null
) {
    val repo = remember { AuthRepository(context) }
    val scope = rememberCoroutineScope()
    val checker = remember { EnforcementPermissionChecker(context) }
    val store = remember { SecureSessionStore(context) }

    var permissionsChecked by remember { mutableStateOf(false) }
    var allPermissionsGranted by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        // Brief splash display for brand visibility
        delay(800)

        // First-run setup wizard takes precedence over the normal flow.
        if (!store.setupWizardCompleted()) {
            onWizardNeeded?.invoke()
            return@LaunchedEffect
        }

        // Wizard already completed — route to the role-based dashboard.
        onWizardComplete?.invoke()
        return@LaunchedEffect
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
