package com.anchorguard.client.ui.screens

import android.content.Context
import android.os.Vibrator
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.anchorguard.client.auth.data.DevicePolicyRepository
import com.anchorguard.client.auth.data.SosEventResponse
import com.anchorguard.client.data.location.LocationRepository
import com.anchorguard.client.data.local.entity.Location
import com.anchorguard.client.network.NetworkConfig
import com.anchorguard.client.network.NetworkResult
import com.anchorguard.client.ui.components.AnchorSnackbarHost
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.material3.Scaffold
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Sos
import androidx.compose.material.icons.filled.Warning

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SOSScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val config = NetworkConfig(context)

    var deviceId by remember { mutableStateOf<String?>(null) }
    var isSending by remember { mutableStateOf(false) }
    var sosSent by remember { mutableStateOf(false) }
    var lastLocation by remember { mutableStateOf<Location?>(null) }

    LaunchedEffect(Unit) {
        scope.launch {
            deviceId = config.deviceId()
            if (deviceId != null) {
                try {
                    val loc = withContext(Dispatchers.Default) {
                        LocationRepository.init(context.applicationContext).getLatestLocation(deviceId!!)
                    }
                    lastLocation = loc
                } catch (e: Exception) {
                    Log.w("SOSScreen", "Failed to load last location", e)
                }
            }
        }
    }

    fun onSosPressed() {
        val device = deviceId ?: return
        isSending = true
        sosSent = false
        scope.launch {
            val lat = lastLocation?.latitude
            val lon = lastLocation?.longitude
            val acc = lastLocation?.accuracyMeters
            val result = withContext(Dispatchers.Default) {
                DevicePolicyRepository(context.applicationContext).sendSos(device, lat, lon, acc)
            }
            isSending = false
            when (result) {
                is NetworkResult.Success -> {
                    sosSent = true
                    // Vibrate device
                    try {
                        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                            vibrator.vibrate(android.os.VibrationEffect.createOneShot(1000, android.os.VibrationEffect.DEFAULT_AMPLITUDE))
                        } else {
                            @Suppress("DEPRECATION")
                            vibrator.vibrate(1000)
                        }
                    } catch (e: Exception) {
                        Log.w("SOSScreen", "Vibrate failed", e)
                    }
                    scope.launch {
                        snackbarHostState.showSnackbar("SOS alert sent with location", duration = SnackbarDuration.Long)
                    }
                }
                is NetworkResult.Failure -> {
                    scope.launch {
                        snackbarHostState.showSnackbar("Failed to send SOS: ${result.error.userMessage()}", duration = SnackbarDuration.Long)
                    }
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { AnchorSnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("SOS Emergency") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                ),
                navigationIcon = {
                    androidx.compose.material3.IconButton(onClick = onBack) {
                        Icon(imageVector = androidx.compose.material.icons.filled.ArrowBack.Default, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Emergency SOS",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Press and hold to send an emergency alert to your parent with your current location.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.TextAlign.Center
            )
            Spacer(modifier = Modifier.height(32.dp))

            if (lastLocation != null) {
                Text(
                    text = "Last known location: ${lastLocation!!.latitude.toString().take(6)}, ${lastLocation!!.longitude.toString().take(6)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
            } else {
                Text(
                    text = "No recent location available — SOS will be sent without coordinates.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            Button(
                onClick = { onSosPressed() },
                enabled = !isSending && deviceId != null,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .width(260.dp)
                    .height(64.dp)
            ) {
                if (isSending) {
                    Text(text = "Sending SOS...")
                } else if (sosSent) {
                    Icon(imageVector = androidx.compose.material.icons.filled.Check.Default, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "SOS Sent")
                } else {
                    Icon(imageVector = androidx.compose.material.icons.filled.Sos.Default, contentDescription = "SOS")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "SOS — Send Emergency Alert", style = MaterialTheme.typography.titleLarge)
                }
            }

            if (sosSent) {
                Spacer(modifier = Modifier.height(16.dp))
                androidx.compose.material3.TextButton(
                    onClick = { sosSent = false }
                ) {
                    Text(text = "Dismiss")
                }
            }
        }
    }
}
