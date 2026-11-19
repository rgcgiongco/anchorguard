package com.anchorguard.client.ui.screens

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.anchorguard.client.auth.data.AppPolicyResponse
import com.anchorguard.client.auth.data.DevicePolicyResponse
import com.anchorguard.client.auth.data.DevicePolicyRepository
import com.anchorguard.client.enforcement.EnforcementController
import com.anchorguard.client.network.NetworkConfig
import com.anchorguard.client.network.NetworkResult
import com.anchorguard.client.ui.components.AnchorSectionDivider
import com.anchorguard.client.ui.theme.AnchorColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.material3.Scaffold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerAppLimitsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val config = NetworkConfig(context)

    var deviceId by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var installedApps by remember { mutableStateOf<List<AppPolicyResponse>>(emptyList()) }
    var appPolicies by remember { mutableStateOf<Map<String, AppPolicyResponse>>(emptyMap()) }

    fun loadApps(deviceId: String) {
        isLoading = true
        scope.launch {
            val result = withContext(Dispatchers.Default) {
                DevicePolicyRepository(context.applicationContext).fetchInstalledApps(deviceId)
            }
            isLoading = false
            when (result) {
                is NetworkResult.Success<*> -> {
                    installedApps = (result as NetworkResult.Success<List<AppPolicyResponse>>).data
                    appPolicies = installedApps.associate { it.packageName to it }
                }
                is NetworkResult.Failure -> {
                    // ignore
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        scope.launch {
            deviceId = config.deviceId()
            if (deviceId != null) {
                loadApps(deviceId!!)
            }
        }
    }

    fun saveAll() {
        scope.launch {
            for (app in installedApps) {
                val policy = appPolicies[app.packageName]
                val result = withContext(Dispatchers.Default) {
                    val isAllowed = policy?.isAllowed ?: true
                    val dailyLimit = policy?.dailyLimitMinutes
                    val internet = if (policy?.internetAccess == "on") "on" else "off"
                    DevicePolicyRepository(context.applicationContext).updateAppPolicy(
                        deviceId!!,
                        app.packageName,
                        app.appName,
                        isAllowed,
                        dailyLimit,
                        internet
                    )
                }
                when (result) {
                    is NetworkResult.Success<*> -> {
                        // ok
                    }
                    is NetworkResult.Failure -> {
                        scope.launch {
                            snackbarHostState.showSnackbar("Failed to save limits for ${app.appName}", duration = SnackbarDuration.Long)
                        }
                    }
                }
            }
            scope.launch {
                snackbarHostState.showSnackbar("All limits saved")
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("App Limits") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                ),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            painter = ColorPainter(MaterialTheme.colorScheme.primary),
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { saveAll() },
                        enabled = deviceId != null && !isLoading
                    ) {
                        Text("Save")
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
                text = "Set daily time limits and internet access per app.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            AnchorSectionDivider()
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.fillMaxWidth().align(Alignment.CenterHorizontally))
            } else if (installedApps.isEmpty()) {
                Text(
                    text = "No apps found",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth().align(Alignment.CenterHorizontally)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(installedApps, key = { it.packageName }) { app ->
                        val policy = appPolicies[app.packageName]
                        val limit = policy?.dailyLimitMinutes
                        var limitText by remember { mutableStateOf(limit?.toString() ?: "") }
                        var internetEnabled by remember { mutableStateOf(policy?.internetAccess == "on") }

                        LaunchedEffect(policy) {
                            limitText = limit?.toString() ?: ""
                            internetEnabled = policy?.internetAccess == "on"
                        }

                        ListItem(
                            modifier = Modifier.fillMaxWidth(),
                            headlineContent = {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = app.appName ?: app.packageName, style = MaterialTheme.typography.titleMedium)
                                    Text(
                                        text = app.packageName,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            },
                            leadingContent = {
                                Icon(
                                    painter = ColorPainter(MaterialTheme.colorScheme.surfaceContainer),
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp)
                                )
                            },
                            trailingContent = {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.End,
                                    verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = limitText,
                                        onValueChange = { limitText = it },
                                        label = { Text("Limit (min)") },
                                        modifier = Modifier.width(80.dp)
                                    )
                                    Switch(
                                        checked = internetEnabled,
                                        onCheckedChange = { internetEnabled = it }
                                    )
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}