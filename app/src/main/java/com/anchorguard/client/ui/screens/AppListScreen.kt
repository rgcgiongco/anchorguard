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
import androidx.compose.ui.graphics.painter.ImagePainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.anchorguard.client.auth.data.AppPolicyResponse
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search

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
    var searchQuery by remember { mutableStateOf("") }
    var appPolicies by remember { mutableStateOf<Map<String, AppPolicyResponse>>(emptyMap()) }
    var installedApps by remember { mutableStateOf<List<InstalledAppInfo>>(emptyList()) }

    val filteredApps = installedApps.filter {
        searchQuery.isBlank() ||
            it.appName.lowercase().contains(searchQuery.lowercase()) ||
            it.packageName.lowercase().contains(searchQuery.lowercase())
    }

    val blockedCount = appPolicies.values.count { !it.isAllowed }

    LaunchedEffect(Unit) {
        scope.launch {
            deviceId = config.deviceId()
            if (deviceId != null) {
                loadApps(deviceId!!)
            }
        }
    }

    LaunchedEffect(refresh) {
        if (deviceId != null) {
            loadApps(deviceId!!)
        }
    }

    fun loadApps(deviceId: String) {
        isLoading = true
        scope.launch {
            val installed = withContext(Dispatchers.Default) {
                getInstalledApps(context)
            }
            val result = withContext(Dispatchers.Default) {
                fetchPolicies(deviceId)
            }
            isLoading = false
            installedApps = installed
            when (result) {
                is NetworkResult.Success -> {
                    appPolicies = result.data.apps.associateBy { it.packageName }
                }
                is NetworkResult.Failure -> {
                    // Continue with installed apps, policies will be empty
                }
            }
        }
    }

    suspend fun fetchPolicies(deviceId: String): NetworkResult<DevicePolicyResponse> {
        val repo = DevicePolicyRepository(context.applicationContext)
        return repo.fetchAppPolicies(deviceId)
    }

    fun onAllowChanged(packageName: String, allowed: Boolean) {
        val current = appPolicies[packageName]
        val appName = current?.appName ?: installedApps.find { it.packageName == packageName }?.appName ?: packageName
        val newPolicy = AppPolicyResponse(
            id = current?.id ?: "",
            deviceId = deviceId ?: "",
            packageName = packageName,
            appName = appName,
            isAllowed = allowed,
            dailyLimitMinutes = current?.dailyLimitMinutes,
            blockSchedule = current?.blockSchedule,
            internetAccess = current?.internetAccess ?: "on",
            updatedAt = current?.updatedAt ?: ""
        )
        appPolicies = appPolicies.toMutableMap().apply { put(packageName, newPolicy) }.toMap()
        scope.launch {
            savePolicy(packageName, newPolicy)
        }
    }

    fun onLimitChanged(packageName: String, limit: Int?) {
        val current = appPolicies[packageName]
        if (current == null) return
        val newPolicy = current.copy(dailyLimitMinutes = limit)
        appPolicies = appPolicies.toMutableMap().apply { put(packageName, newPolicy) }.toMap()
        scope.launch {
            savePolicy(packageName, newPolicy)
        }
    }

    fun onInternetChanged(packageName: String, enabled: Boolean) {
        val current = appPolicies[packageName]
        if (current == null) return
        val newPolicy = current.copy(internetAccess = if (enabled) "on" else "off")
        appPolicies = appPolicies.toMutableMap().apply { put(packageName, newPolicy) }.toMap()
        scope.launch {
            savePolicy(packageName, newPolicy)
        }
    }

    suspend fun savePolicy(packageName: String, policy: AppPolicyResponse) {
        val repo = DevicePolicyRepository(context.applicationContext)
        val result = repo.updateAppPolicy(
            deviceId = deviceId ?: "",
            packageName = packageName,
            appName = policy.appName,
            isAllowed = policy.isAllowed,
            dailyLimitMinutes = policy.dailyLimitMinutes,
            internetAccess = policy.internetAccess
        )
        when (result) {
            is NetworkResult.Success -> {
                // Sync blocked packages to local enforcement engine
                val blocked = appPolicies.values.filter { !it.isAllowed }.map { it.packageName }.toSet()
                EnforcementController.syncBlockedPackages(context, blocked)
                scope.launch {
                    snackbarHostState.showSnackbar("Policy saved for $packageName")
                }
            }
            is NetworkResult.Failure -> {
                scope.launch {
                    snackbarHostState.showSnackbar("Failed to save policy: ${result.error.userMessage()}", duration = SnackbarDuration.Long)
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("App allow list") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                ),
                navigationIcon = {
                    IconButton(onClick = onBack) {
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AnchorTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = "Search apps",
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = androidx.compose.material.icons.filled.Search.Default
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
                    Icon(imageVector = androidx.compose.material.icons.filled.Refresh.Default, contentDescription = "Refresh")
                }
            }
            AnchorSectionDivider()
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (filteredApps.isEmpty()) {
                Text(
                    text = "No apps found",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    state = rememberLazyListState()
                ) {
                    items(filteredApps, key = { it.packageName }) { app ->
                        val policy = appPolicies[app.packageName]
                        val isAllowed = policy?.isAllowed ?: true
                        val currentLimit = policy?.dailyLimitMinutes
                        var limitText by remember { mutableStateOf(currentLimit?.toString() ?: "") }
                        var internetEnabled by remember { mutableStateOf(policy?.internetAccess == "on") }

                        LaunchedEffect(policy) {
                            limitText = currentLimit?.toString() ?: ""
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
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Switch(
                                        checked = isAllowed,
                                        onCheckedChange = { onAllowChanged(app.packageName, it) },
                                        colors = androidx.compose.material3.SwitchDefaults.colors(
                                            uncheckedColor = if (!isAllowed) MaterialTheme.colorScheme.error else Color.Transparent
                                        )
                                    )
                                    if (isAllowed) {
                                        Row(modifier = Modifier.fillMaxWidth()) {
                                            Text(
                                                text = "Daily limit:",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            OutlinedTextField(
                                                value = limitText,
                                                onValueChange = {
                                                    limitText = it
                                                    if (it.isNotBlank()) {
                                                        val parsed = it.toIntOrNull()
                                                        if (parsed != null) {
                                                            onLimitChanged(app.packageName, parsed)
                                                        }
                                                    } else {
                                                        onLimitChanged(app.packageName, null)
                                                    }
                                                },
                                                singleLine = true,
                                                modifier = Modifier.width(80.dp),
                                                label = { Text("min") }
                                            )
                                        }
                                        Switch(
                                            checked = internetEnabled,
                                            onCheckedChange = { onInternetChanged(app.packageName, it) }
                                        )
                                        Text(
                                            text = "Internet",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

data class InstalledAppInfo(
    val packageName: String,
    val appName: String,
    val icon: ImageBitmap? = null
)

fun getInstalledApps(context: Context): List<InstalledAppInfo> {
    val pm = context.packageManager
    val apps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
    return apps.map { appInfo ->
        val appName = try {
            pm.getApplicationLabel(appInfo)?.toString() ?: appInfo.packageName
        } catch (e: Exception) {
            appInfo.packageName
        }
        val icon = try {
            val drawable: Drawable = appInfo.loadIcon(pm)
            drawableToBitmap(drawable)?.asImageBitmap()
        } catch (e: Exception) {
            null
        }
        InstalledAppInfo(
            packageName = appInfo.packageName,
            appName = appName,
            icon = icon
        )
    }.sortedBy { it.appName.lowercase() }
}

private fun drawableToBitmap(drawable: Drawable): Bitmap? {
    return if (drawable is BitmapDrawable) {
        drawable.bitmap
    } else {
        val width = drawable.intrinsicWidth.coerceAtLeast(1)
        val height = drawable.intrinsicHeight.coerceAtLeast(1)
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        bitmap
    }
}
