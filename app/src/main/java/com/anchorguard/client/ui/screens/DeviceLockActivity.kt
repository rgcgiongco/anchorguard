package com.anchorguard.client.ui.screens

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.anchorguard.client.MainActivity
import com.anchorguard.client.auth.data.DevicePolicyRepository
import com.anchorguard.client.auth.security.PinSecurityManager
import com.anchorguard.client.enforcement.EnforcementPermissionChecker
import com.anchorguard.client.network.NetworkConfig
import com.anchorguard.client.network.NetworkResult
import com.anchorguard.client.ui.theme.AnchorGuardTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DeviceLockActivity : androidx.fragment.app.FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AnchorGuardTheme {
                DeviceLockScreen(
                    onUnlock = { pin ->
                        val result = verifyPinAndUnlock(pin)
                        if (result) {
                            finish()
                        }
                    }
                )
            }
        }
    }

    override fun onBackPressed() {
        // Prevent dismissing lock screen via back button
    }

    private fun verifyPinAndUnlock(pin: String): Boolean {
        val appCtx = applicationContext
        val pinManager = PinSecurityManager(appCtx)
        val result = pinManager.verifyPin(pin)
        when (result) {
            is PinSecurityManager.PinVerifyResult.Success -> {
                // Unlock on backend
                val deviceId = NetworkConfig(appCtx).deviceId()
                if (deviceId != null) {
                    kotlinx.coroutines.GlobalScope.launch {
                        try {
                            val repo = DevicePolicyRepository(appCtx)
                            val unlockResult = withContext(Dispatchers.Default) {
                                repo.unlockDevice(deviceId)
                            }
                            when (unlockResult) {
                                is NetworkResult.Success -> {
                                    Log.i("DeviceLockActivity", "Device unlocked on backend")
                                }
                                is NetworkResult.Failure -> {
                                    Log.w("DeviceLockActivity", "Failed to unlock on backend: ${unlockResult.error.userMessage()}")
                                }
                            }
                        } catch (e: Exception) {
                            Log.w("DeviceLockActivity", "Unlock error", e)
                        }
                    }
                }
                return true
            }
            else -> {
                Log.w("DeviceLockActivity", "Incorrect PIN")
                return false
            }
        }
    }

    companion object {
        fun start(context: Context, reason: String?) {
            val intent = Intent(context, DeviceLockActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                reason?.let { putExtra("lock_reason", it) }
            }
            context.startActivity(intent)
        }
    }
}

@Composable
fun DeviceLockScreen(onUnlock: (String) -> Unit) {
    val context = LocalContext.current
    var pin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }
    var isChecking by remember { mutableStateOf(false) }

    val reason = context.intent.getStringExtra("lock_reason")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
    ) {
        Text(
            text = "Device Locked",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "This device has been locked by your parent.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        if (reason != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = reason,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.height(32.dp))
        TextField(
            value = pin,
            onValueChange = { pin = it },
            label = { Text("Enter parent PIN") },
            modifier = Modifier.fillMaxWidth().width(300.dp),
            singleLine = true,
            isError = error,
            visualTransformation = androidx.compose.ui.text.input.VisualTransformation.Password()
        )
        if (error) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Incorrect PIN. Try again.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                if (pin.isNotBlank()) {
                    isChecking = true
                    onUnlock(pin)
                    isChecking = false
                } else {
                    error = true
                }
            },
            enabled = pin.isNotBlank() && !isChecking
        ) {
            if (isChecking) {
                Text(text = "Verifying...")
            } else {
                Text(text = "Unlock")
            }
        }
    }
}
