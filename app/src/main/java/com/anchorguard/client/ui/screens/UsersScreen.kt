package com.anchorguard.client.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.anchorguard.client.auth.data.DeviceRepository
import com.anchorguard.client.auth.data.DeviceResponse
import com.anchorguard.client.network.NetworkResult
import com.anchorguard.client.ui.components.AnchorSnackbarHost
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.material3.Scaffold
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsersScreen(onDeviceSelected: ((String) -> Unit)? = null) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val repo = remember { DeviceRepository(context) }

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

    LaunchedEffect(Unit) {
        loadDevices()
    }

    LaunchedEffect(refresh) {
        loadDevices()
    }

    fun loadDevices() {
        isLoading = true
        scope.launch {
            val result = withContext(Dispatchers.Default) {
                repo.listDevices()
            }
            isLoading = false
            when (result) {
                is NetworkResult.Success -> {
                    devices = result.data
                }
                is NetworkResult.Failure -> {
                    scope.launch {
                        snackbarHostState.showSnackbar("Failed to load devices: ${result.error.userMessage()}", duration = SnackbarDuration.Long)
                    }
                }
            }
        }
    }

    fun onAddDevice() {
        if (newDeviceName.isBlank()) {
            scope.launch {
                snackbarHostState.showSnackbar("Please enter a child name", duration = SnackbarDuration.Long)
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
                is NetworkResult.Success -> {
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
                is NetworkResult.Success -> {
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
                is NetworkResult.Success -> {
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
                        Icon(imageVector = androidx.compose.material.icons.filled.ArrowBack.Default, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { refresh = !refresh }
                    ) {
                        Icon(imageVector = androidx.compose.material.icons.filled.Refresh.Default, contentDescription = "Refresh")
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

            Button(
                onClick = { showAddDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors()
            ) {
                Icon(imageVector = androidx.compose.material.icons.filled.Add.Default, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Add Child Device")
            }

            Spacer(Modifier.height(16.dp))

            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (devices.isEmpty()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surfaceContainer
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
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

                        ListItem(
                            modifier = Modifier.fillMaxWidth(),
                            headlineContent = {
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
                            },
                            supportingContent = {
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
                            },
                            trailingContent = {
                                androidx.compose.foundation.layout.Row(
                                    modifier = Modifier.width(100.dp),
                                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
                                ) {
                                    IconButton(
                                        onClick = {
                                            renameDeviceId = device.id
                                            renameDeviceName = device.name ?: ""
                                            showRenameDialog = true
                                        }
                                    ) {
                                        Icon(imageVector = androidx.compose.material.icons.filled.Edit.Default, contentDescription = "Rename")
                                    }
                                    IconButton(
                                        onClick = {
                                            deleteDeviceId = device.id
                                            deleteDeviceName = device.name ?: "this device"
                                            showDeleteDialog = true
                                        }
                                    ) {
                                        Icon(imageVector = androidx.compose.material.icons.filled.Delete.Default, contentDescription = "Remove")
                                    }
                                }
                            },
                            onClick = {
                                onDeviceSelected?.invoke(device.id)
                            }
                        )
                    }
                }
            }
        }
    }

    // Add Device Dialog
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add Child Device") },
            text = { Text("Enter a name for the child device.") },
            confirmButton = {
                TextButton(onClick = { onAddDevice() }) { Text("Add") }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("Cancel") }
            }
        ) {
            OutlinedTextField(
                value = newDeviceName,
                onValueChange = { newDeviceName = it },
                label = { Text("Child name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }
    }

    // Rename Dialog
    if (showRenameDialog && renameDeviceId != null) {
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            title = { Text("Rename Device") },
            text = { Text("Enter a new name for ${renameDeviceName}.") },
            confirmButton = {
                TextButton(onClick = { onRenameSave() }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = false }) { Text("Cancel") }
            }
        ) {
            OutlinedTextField(
                value = renameDeviceName,
                onValueChange = { renameDeviceName = it },
                label = { Text("Device name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }
    }

    // Delete Confirm Dialog
    if (showDeleteDialog && deleteDeviceId != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Remove Device") },
            text = { Text("Unlink \"$deleteDeviceName\"? This cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = { onDeleteConfirm() },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) { Text("Remove") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            }
        )
    }
}
