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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.anchorguard.client.auth.data.DevicePolicyRepository
import com.anchorguard.client.network.NetworkResult
import com.anchorguard.client.ui.theme.AnchorColors
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SOSScreen(
    onBack: () -> Unit,
    onSosPressed: () -> Unit
) {
    val context = LocalContext.current
    val isSosActive = remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        // Observe SOS state
    }

    androidx.compose.material3.Scaffold(
        topBar = {
            androidx.compose.material3.TopAppBar(
                title = { Text("SOS") },
                colors = androidx.compose.material3.TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                ),
                navigationIcon = {
                    androidx.compose.material3.IconButton(onClick = onBack) {
                        androidx.compose.material3.Icon(
                            painter = ColorPainter(MaterialTheme.colorScheme.primary),
                            contentDescription = "Back"
                        )
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Emergency SOS",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
                )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Press and hold the SOS button to alert your emergency contacts.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = {
                    isSosActive.value = true
                    onSosPressed()
                },
                enabled = !isSosActive.value,
                colors = if (isSosActive.value) ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ) else ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text(
                    text = if (isSosActive.value) "SOS Active" else "Send SOS",
                    style = MaterialTheme.typography.labelLarge,
                    color = if (isSosActive.value) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onError
                )
            }
            if (isSosActive.value) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "SOS alert sent. Your emergency contacts have been notified.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    )
            }
        }
    }
}