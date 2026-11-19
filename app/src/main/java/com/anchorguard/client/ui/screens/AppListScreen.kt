package com.anchorguard.client.ui.screens

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.graphics.asImageBitmap
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
import com.anchorguard.client.ui.components.AnchorTextField
import com.anchorguard.client.ui.theme.AnchorColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.material3.Scaffold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppListScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val config = NetworkConfig(context)

    var deviceId by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var refresh by remember { mutableStateOf(false) }

    var apps by remember { mutableStateOf<List<AppInfo>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }
    var blockedCount by remember { mutableStateOf(0) }

    val filteredApps = apps.filter { it.name.contains(searchQuery, ignoreCase = true) }

    fun loadApps(deviceId: String) {
        isLoading = true
        scope.launch {
            val result = withContext(Dispatchers.Default) {
                DevicePolicyRepository(context.applicationContext).fetchInstalledApps(deviceId)
            }
            isLoading = false
            when (result) {
                is NetworkResult.Success<*> -> {
                    val resp = (result as NetworkResult.Success<List<AppPolicyResponse>>).data
                    apps = resp.map { it.toAppInfo() }
                    blockedCount = resp.count { !it.isAllowed }
                }
                is NetworkResult.Failure -> {
                    // ignore
                }
            }
        }
    }

    fun savePolicy(app: AppInfo, isAllowed: Boolean) {
        scope.launch {
            val result = withContext(Dispatchers.Default) {
                DevicePolicyRepository(context.applicationContext).updateAppPolicy(
                    deviceId = deviceId!!,
                    packageName = app.packageName,
                    appName = app.name,
                    isAllowed = isAllowed,
                    dailyLimitMinutes = null,
                    internetAccess = null
                )
            }
            when (result) {
                is NetworkResult.Success<*> -> {
                    scope.launch {
                        snackbarHostState.showSnackbar("Policy updated")
                    }
                    refresh = !refresh
                }
                is NetworkResult.Failure -> {
                    scope.launch {
                        snackbarHostState.showSnackbar("Failed to update policy: ${result.error.userMessage()}", duration = SnackbarDuration.Long)
                    }
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("App Management") },
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
            AnchorTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = "Search apps",
                modifier = Modifier.fillMaxWidth()
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${filteredApps.size} apps${if (blockedCount > 0) ", $blockedCount blocked" else ""}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                IconButton(
                    onClick = { refresh = !refresh }
                ) {
                    Icon(
                        painter = ColorPainter(MaterialTheme.colorScheme.primary),
                        contentDescription = "Refresh"
                    )
                }
            }
            AnchorSectionDivider()
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.fillMaxWidth().align(Alignment.CenterHorizontally))
            } else if (filteredApps.isEmpty()) {
                Text(
                    text = "No apps found",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    state = rememberLazyListState()
                ) {
                    items(filteredApps, key = { it.packageName }) { app ->
                        ListItem(
                            modifier = Modifier.fillMaxWidth(),
                            headlineContent = {
                                Text(
                                    text = app.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            },
                            leadingContent = {
                                Icon(
                                    painter = ColorPainter(MaterialTheme.colorScheme.surfaceContainer),
                                    contentDescription = app.name
                                )
                            },
                            trailingContent = {
                                Switch(
                                    checked = app.isAllowed,
                                    onCheckedChange = { allowed ->
                                        savePolicy(app, allowed)
                                    }
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

private fun AppPolicyResponse.toAppInfo(): AppInfo {
    return AppInfo(
        packageName = packageName,
        name = appName ?: packageName,
        isAllowed = isAllowed
    )
}

data class AppInfo(
    val packageName: String,
    val name: String,
    val isAllowed: Boolean
)