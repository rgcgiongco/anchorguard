package com.anchorguard.client.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.anchorguard.client.R
import com.anchorguard.client.auth.data.DeviceRepository
import com.anchorguard.client.auth.data.DeviceResponse
import com.anchorguard.client.data.local.UserRepository
import com.anchorguard.client.model.SubscriptionTier
import com.anchorguard.client.network.NetworkConfig
import com.anchorguard.client.network.NetworkResult
import com.anchorguard.client.ui.components.AnchorSnackbarHost
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsersScreen(onDeviceSelected: ((String) -> Unit)? = null) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val config = NetworkConfig(context)
    val repo = remember { DeviceRepository(context) }
    val userRepo = remember { UserRepository(context) }
    val userTier by remember { mutableStateOf(userRepo.getUserTier()) }

    var devices by remember { mutableStateOf<List<DeviceResponse>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var refresh by remember { mutableStateOf(false) }

    var showAddDialog by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var renameDeviceId by remember { mutableStateOf<String?>(null) }
    var renameDeviceName by remember { mutableStateOf("") }
    var newDeviceName by remember { mutableStateOf("") }
    var deleteDeviceId by remember { mutableStateOf<String?>(null) }
    var deleteDeviceName by remember { mutableStateOf("") }

    fun loadDevices() {
        isLoading = true
        scope.launch {
            val result = withContext(Dispatchers.Default) {
                repo.listDevices()
            }
            isLoading = false
            when (result) {
                is NetworkResult.Success<*> -> {
                    devices = (result as NetworkResult.Success<List<DeviceResponse>>).data
                }
                is NetworkResult.Failure -> {
                    scope.launch {
                        snackbarHostState.showSnackbar("Failed to load devices: ${result.error.userMessage()}", duration = SnackbarDuration.Long)
                    }
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        loadDevices()
    }

    LaunchedEffect(refresh) {
        loadDevices()
    }

    fun onAddDevice() {
        if (newDeviceName.isBlank()) {
            scope.launch {
                snackbarHostState.showSnackbar("Please enter a child name", duration = SnackbarDuration.Long)
            }
            return
        }
        // Free tier device limit check
        if (userTier.tier == SubscriptionTier.FREE && devices.size >= userTier.deviceLimit) {
            scope.launch {
                snackbarHostState.showSnackbar(context.getString(R.string.free_tier_device_limit_reached), duration = SnackbarDuration.Long)
            }
            return
        }
        isLoading = true
        showAddDialog = false
        scope.launch {
            val result = withContext(Dispatchers.Default) {
                repo.createChildDevice(newDeviceName, null, null)
            }
            isLoading = false
            when (result) {
                is NetworkResult.Success<*> -> {
                    newDeviceName = ""
                    scope.launch {
                        snackbarHostState.showSnackbar("Child device added")
                    }
                    refresh = !refresh
                }
                is NetworkResult.Failure -> {
                    scope.launch {
                        snackbarHostState.showSnackbar("Failed to add device: ${result.error.userMessage()}", duration = SnackbarDuration.Long)
                    }
                }
            }
        }
    }

    fun onRenameSave() {
        val id = renameDeviceId ?: return
        if (renameDeviceName.isBlank()) {
            scope.launch {
                snackbarHostState.showSnackbar("Please enter a name", duration = SnackbarDuration.Long)
            }
            return
        }
        isLoading = true
        showRenameDialog = false
        scope.launch {
            val result = withContext(Dispatchers.Default) {
                repo.renameDevice(id, renameDeviceName)
            }
            isLoading = false
            when (result) {
                is NetworkResult.Success<*> -> {
                    renameDeviceId = null
                    renameDeviceName = ""
                    scope.launch {
                        snackbarHostState.showSnackbar("Device renamed")
                    }
                    refresh = !refresh
                }
                is NetworkResult.Failure -> {
                    scope.launch {
                        snackbarHostState.showSnackbar("Failed to rename: ${result.error.userMessage()}", duration = SnackbarDuration.Long)
                    }
                }
            }
        }
    }

    fun onDeleteConfirm() {
        val id = deleteDeviceId ?: return
        isLoading = true
        showDeleteDialog = false
        scope.launch {
            val result = withContext(Dispatchers.Default) {
                repo.removeDevice(id)
            }
            isLoading = false
            when (result) {
                is NetworkResult.Success<*> -> {
                    deleteDeviceId = null
                    deleteDeviceName = ""
                    scope.launch {
                        snackbarHostState.showSnackbar("Device removed")
                    }
                    refresh = !refresh
                }
                is NetworkResult.Failure -> {
                    scope.launch {
                        snackbarHostState.showSnackbar("Failed to remove: ${result.error.userMessage()}", duration = SnackbarDuration.Long)
                    }
                }
            }
        }
    }

    fun formatLastSeen(iso: String?): String {
        if (iso == null || iso.isBlank()) return "Never"
        return iso
    }

    Scaffold(
        snackbarHost = { AnchorSnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Child Profiles") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                ),
                navigationIcon = {
                    IconButton(onClick = { /* handled by nav host */ }) {
                        Icon(
                            painter = ColorPainter(MaterialTheme.colorScheme.primary),
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { refresh = !refresh }
                    ) {
                        Icon(
                            painter = ColorPainter(MaterialTheme.colorScheme.primary),
                            contentDescription = "Refresh"
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
                text = "Manage linked child devices",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Each card represents a child profile with its own settings and limits.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))

            val atDeviceLimit = userTier.tier == SubscriptionTier.FREE && devices.size >= userTier.deviceLimit
            Button(
                onClick = { if (!atDeviceLimit) showAddDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(),
                enabled = !atDeviceLimit
            ) {
                Icon(
                    painter = ColorPainter(MaterialTheme.colorScheme.primary),
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (atDeviceLimit) "Upgrade to add more devices" else "Add Child Device",
                    style = if (atDeviceLimit) MaterialTheme.typography.labelLarge.copy(color = MaterialTheme.colorScheme.error) else MaterialTheme.typography.bodyMedium
                )
            }
            if (atDeviceLimit) {
                Text(
                    text = context.getString(R.string.free_tier_device_limit_reached),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(Modifier.height(8.dp))
            }

            Spacer(Modifier.height(16.dp))

            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.fillMaxWidth().align(Alignment.CenterHorizontally))
            } else if (devices.isEmpty()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surfaceContainer
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "No child devices linked",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Add a child device to get started.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(devices, key = { it.id }) { device ->
                        val isOnline = device.isOnline
                        val statusText = if (isOnline) "Online" else "Offline"
                        val statusColor = if (isOnline) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = device.name ?: "Unnamed Device",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${device.model ?: "Unknown model"} ${device.manufacturer ?: ""}".trimEnd(),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = statusText,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = statusColor
                                )
                                Text(
                                    text = "Last seen: ${formatLastSeen(device.lastSeen)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                IconButton(
                                    onClick = {
                                        renameDeviceId = device.id
                                        renameDeviceName = device.name ?: ""
                                        showRenameDialog = true
                                    }
                                ) {
                                    Icon(
                                        painter = ColorPainter(MaterialTheme.colorScheme.primary),
                                        contentDescription = "Rename"
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        deleteDeviceId = device.id
                                        deleteDeviceName = device.name ?: "this device"
                                        showDeleteDialog = true
                                    }
                                ) {
                                    Icon(
                                        painter = ColorPainter(MaterialTheme.colorScheme.error),
                                        contentDescription = "Remove"
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Inline dialogs (shown when flags are set)
    if (showAddDialog) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surfaceContainer,
            shape = MaterialTheme.shapes.medium
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Add Child Device",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Enter a name for the child device.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                androidx.compose.material3.OutlinedTextField(
                    value = newDeviceName,
                    onValueChange = { newDeviceName = it },
                    label = { Text("Child name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    androidx.compose.material3.TextButton(
                        onClick = { onAddDevice() }
                    ) { Text("Add") }
                    androidx.compose.material3.TextButton(
                        onClick = { showAddDialog = false }
                    ) { Text("Cancel") }
                }
            }
        }
    }

    if (showRenameDialog && renameDeviceId != null) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surfaceContainer,
            shape = MaterialTheme.shapes.medium
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Rename Device",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Enter a new name for ${renameDeviceName}.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                androidx.compose.material3.OutlinedTextField(
                    value = renameDeviceName,
                    onValueChange = { renameDeviceName = it },
                    label = { Text("Device name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    androidx.compose.material3.TextButton(
                        onClick = { onRenameSave() }
                    ) { Text("Save") }
                    androidx.compose.material3.TextButton(
                        onClick = { showRenameDialog = false }
                    ) { Text("Cancel") }
                }
            }
        }
    }

    if (showDeleteDialog && deleteDeviceId != null) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surfaceContainer,
            shape = MaterialTheme.shapes.medium
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Remove Device",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Unlink \"$deleteDeviceName\"? This cannot be undone.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    androidx.compose.material3.TextButton(
                        onClick = { onDeleteConfirm() },
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                    ) { Text("Remove") }
                    androidx.compose.material3.TextButton(
                        onClick = { showDeleteDialog = false }
                    ) { Text("Cancel") }
                }
            }
        }
    }
}