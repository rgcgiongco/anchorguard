package com.anchorguard.client.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.graphics.painter.ImagePainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.anchorguard.client.auth.data.AppPolicyResponse
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Save

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerAppLimitsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val config = NetworkConfig(context)

    var deviceId by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var appPolicies by remember { mutableStateOf<Map<String, AppPolicyResponse>>(emptyMap()) }
    var installedApps by remember { mutableStateOf<List<InstalledAppInfo>>(emptyList()) }

    LaunchedEffect(Unit) {
        scope.launch {
            deviceId = config.deviceId()
            if (deviceId != null) {
                loadApps(deviceId!!)
            }
        }
    }

    fun loadApps(deviceId: String) {
        isLoading = true
        scope.launch {
            val installed = withContext(Dispatchers.Default) {
                getInstalledApps(context)
            }
            val result = withContext(Dispatchers.Default) {
                DevicePolicyRepository(context.applicationContext).fetchAppPolicies(deviceId)
            }
            isLoading = false
            installedApps = installed
            when (result) {
                is NetworkResult.Success -> {
                    appPolicies = result.data.apps.associateBy { it.packageName }
                }
                is NetworkResult.Failure -> {
                    // continue with installed apps, policies empty
                }
            }
        }
    }

    fun saveAll() {
        val device = deviceId ?: return
        isLoading = true
        scope.launch {
            val dtoList = appPolicies.values.map { policy ->
                com.anchorguard.client.auth.data.AppPolicyDto(
                    packageName = policy.packageName,
                    appName = policy.appName,
                    isAllowed = policy.isAllowed,
                    dailyLimitMinutes = policy.dailyLimitMinutes,
                    blockSchedule = policy.blockSchedule,
                    internetAccess = policy.internetAccess
                )
            }
            val result = withContext(Dispatchers.Default) {
                DevicePolicyRepository(context.applicationContext).updateDevicePolicy(
                    deviceId = device,
                    settings = null,
                    apps = dtoList
                )
            }
            isLoading = false
            when (result) {
                is NetworkResult.Success -> {
                    appPolicies.values.forEach { policy ->
                        EnforcementController.syncAppDailyLimit(context, policy.packageName, policy.dailyLimitMinutes)
                    }
                    scope.launch {
                        snackbarHostState.showSnackbar("Per-app limits saved")
                    }
                }
                is NetworkResult.Failure -> {
                    scope.launch {
                        snackbarHostState.showSnackbar("Failed to save limits: ${result.error.userMessage()}", duration = SnackbarDuration.Long)
                    }
                }
            }
        }
    }

    fun onLimitChanged(packageName: String, limit: Int?) {
        val current = appPolicies[packageName]
        if (current == null) return
        val newPolicy = current.copy(dailyLimitMinutes = limit)
        appPolicies = appPolicies.toMutableMap().apply { put(packageName, newPolicy) }.toMap()
    }

    fun onInternetChanged(packageName: String, enabled: Boolean) {
        val current = appPolicies[packageName]
        if (current == null) return
        val newPolicy = current.copy(internetAccess = if (enabled) "on" else "off")
        appPolicies = appPolicies.toMutableMap().apply { put(packageName, newPolicy) }.toMap()
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Per-app limits") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                ),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = androidx.compose.material.icons.filled.ArrowBack.Default, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { saveAll() },
                        enabled = deviceId != null && !isLoading
                    ) {
                        Icon(imageVector = androidx.compose.material.icons.filled.Save.Default, contentDescription = "Save")
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
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (installedApps.isEmpty()) {
                Text(
                    text = "No apps found",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.Center)
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
                                    Text(text = app.appName, style = MaterialTheme.typography.titleMedium)
                                    Text(
                                        text = app.packageName,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            },
                            leadingContent = {
                                val painter = app.icon?.let { ImagePainter(it) } ?: ColorPainter(AnchorColors.surfaceContainer)
                                Icon(
                                    painter = painter,
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
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = "Limit:",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        OutlinedTextField(
                                            value = limitText,
                                            onValueChange = {
                                                limitText = it
                                                if (it.isBlank()) {
                                                    onLimitChanged(app.packageName, null)
                                                } else {
                                                    val parsed = it.toIntOrNull()
                                                    if (parsed != null) {
                                                        onLimitChanged(app.packageName, parsed)
                                                    }
                                                }
                                            },
                                            singleLine = true,
                                            modifier = Modifier.width(70.dp),
                                            label = { Text("min") }
                                        )
                                    }
                                    Switch(
                                        checked = internetEnabled,
                                        onCheckedChange = { onInternetChanged(app.packageName, it) },
                                        colors = androidx.compose.material3.SwitchDefaults.colors(
                                            uncheckedColor = if (!internetEnabled) MaterialTheme.colorScheme.error else Color.Transparent
                                        )
                                    )
                                    Text(
                                        text = "Internet",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
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
