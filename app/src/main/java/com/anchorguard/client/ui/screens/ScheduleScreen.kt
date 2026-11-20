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
import androidx.compose.material3.TimePicker
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.material.icons.Icons
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.anchorguard.client.R
import com.anchorguard.client.auth.data.DevicePolicyRepository
import com.anchorguard.client.auth.data.ScheduleResponse
import com.anchorguard.client.auth.data.ScheduleUpdateRequest
import com.anchorguard.client.data.local.UserRepository
import com.anchorguard.client.enforcement.EnforcementController
import com.anchorguard.client.model.SubscriptionTier
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
import com.google.gson.Gson
import com.google.gson.JsonObject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val config = NetworkConfig(context)
    val userRepo = remember { UserRepository(context) }
    val userTier by remember { mutableStateOf(userRepo.getUserTier()) }

    var deviceId by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var scheduleEnabled by remember { mutableStateOf(false) }
    var weeklySchedule by remember { mutableStateOf<Map<String, DaySchedule>>(emptyMap()) }

    val days = listOf("monday", "tuesday", "wednesday", "thursday", "friday", "saturday", "sunday")
    val dayLabels = mapOf(
        "monday" to "Monday", "tuesday" to "Tuesday", "wednesday" to "Wednesday",
        "thursday" to "Thursday", "friday" to "Friday", "saturday" to "Saturday", "sunday" to "Sunday"
    )

    fun loadSchedule(deviceId: String) {
        isLoading = true
        scope.launch {
            val result = withContext(Dispatchers.Default) {
                DevicePolicyRepository(context.applicationContext).fetchSchedule(deviceId)
            }
            isLoading = false
            when (result) {
                is NetworkResult.Success<*> -> {
                    val resp = (result as NetworkResult.Success<ScheduleResponse>).data
                    val json = resp.weeklySchedule
                    if (json != null) {
                        weeklySchedule = parseSchedule(json)
                        scheduleEnabled = true
                    }
                }
                is NetworkResult.Failure -> {
                    // Start with empty schedule
                }
            }
        }
    }

    fun buildScheduleJson(): String {
        val obj = JsonObject()
        for ((day, sched) in weeklySchedule) {
            val dayObj = JsonObject()
            dayObj.addProperty("enabled", sched.enabled)
            dayObj.addProperty("startHour", sched.startHour)
            dayObj.addProperty("startMinute", sched.startMinute)
            dayObj.addProperty("endHour", sched.endHour)
            dayObj.addProperty("endMinute", sched.endMinute)
            obj.add(day, dayObj)
        }
        return Gson().toJson(obj)
    }

    fun saveSchedule() {
        // Free tier: schedules not allowed
        if (userTier.tier == SubscriptionTier.FREE) {
            scope.launch {
                snackbarHostState.showSnackbar(context.getString(R.string.free_tier_schedules_disabled), duration = SnackbarDuration.Long)
            }
            return
        }
        val device = deviceId ?: return
        isLoading = true
        scope.launch {
            val json = buildScheduleJson()
            val result = withContext(Dispatchers.Default) {
                DevicePolicyRepository(context.applicationContext).updateSchedule(device, json)
            }
            isLoading = false
            when (result) {
                is NetworkResult.Success<*> -> {
                    EnforcementController.syncSchedule(context, json)
                    scope.launch {
                        snackbarHostState.showSnackbar("Schedule saved")
                    }
                }
                is NetworkResult.Failure -> {
                    scope.launch {
                        snackbarHostState.showSnackbar("Failed to save schedule: ${result.error.userMessage()}", duration = SnackbarDuration.Long)
                    }
                }
            }
        }
    }

    fun toggleDay(day: String) {
        val current = weeklySchedule[day] ?: DaySchedule(enabled = false)
        weeklySchedule = weeklySchedule.toMutableMap().apply {
            put(day, current.copy(enabled = !current.enabled))
        }.toMap()
    }

    LaunchedEffect(Unit) {
        scope.launch {
            deviceId = config.deviceId()
            if (deviceId != null) {
                loadSchedule(deviceId!!)
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Schedules") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                ),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(painter = androidx.compose.ui.graphics.painter.ColorPainter(MaterialTheme.colorScheme.primary), contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { saveSchedule() },
                        enabled = deviceId != null && !isLoading
                    ) {
                        Icon(painter = androidx.compose.ui.graphics.painter.ColorPainter(MaterialTheme.colorScheme.primary), contentDescription = "Save")
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
            if (userTier.tier == SubscriptionTier.FREE) {
                Text(
                    text = context.getString(R.string.free_tier_schedules_disabled),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
                Text(
                    text = context.getString(R.string.free_tier_upgrade_now),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            } else {
                Switch(
                    checked = scheduleEnabled,
                    onCheckedChange = { scheduleEnabled = it }
                )
                Text(
                    text = "Schedule enforcement",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Enable/disable weekly allowed hours and bedtime windows.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                AnchorSectionDivider()
            }
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.fillMaxWidth().align(Alignment.CenterHorizontally))
            } else {
                Column {
                    days.forEach { day ->
                        val daySchedule = weeklySchedule[day] ?: DaySchedule(enabled = false)
                        ListItem(
                            modifier = Modifier.fillMaxWidth(),
                            headlineContent = {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = dayLabels[day] ?: day,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Text(
                                        text = if (daySchedule.enabled) "Allowed: ${formatTime(daySchedule.startHour, daySchedule.startMinute)} – ${formatTime(daySchedule.endHour, daySchedule.endMinute)}" else "Disabled",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            },
                            leadingContent = {
                                Switch(
                                    checked = daySchedule.enabled,
                                    onCheckedChange = { toggleDay(day) }
                                )
                            },
                            trailingContent = {
                                if (daySchedule.enabled) {
                                    Row(
                                        modifier = Modifier.width(120.dp),
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = "${pad(daySchedule.startHour)}:${pad(daySchedule.startMinute)}",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "–",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "${pad(daySchedule.endHour)}:${pad(daySchedule.endMinute)}",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.onSurface
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

data class DaySchedule(
    val enabled: Boolean = false,
    val startHour: Int = 8,
    val startMinute: Int = 0,
    val endHour: Int = 20,
    val endMinute: Int = 0
)

private fun parseSchedule(json: String): Map<String, DaySchedule> {
    val result = mutableMapOf<String, DaySchedule>()
    try {
        val obj = com.google.gson.JsonParser.parseString(json).asJsonObject
        for (day in listOf("monday", "tuesday", "wednesday", "thursday", "friday", "saturday", "sunday")) {
            val dayObj = obj.getAsJsonObject(day)
            if (dayObj != null) {
                result[day] = DaySchedule(
                    enabled = dayObj.get("enabled").asBoolean,
                    startHour = dayObj.get("startHour").asInt,
                    startMinute = dayObj.get("startMinute").asInt,
                    endHour = dayObj.get("endHour").asInt,
                    endMinute = dayObj.get("endMinute").asInt
                )
            }
        }
    } catch (e: Exception) {
        // ignore parse errors, return empty
    }
    return result
}

private fun formatTime(hour: Int, minute: Int): String {
    return "${pad(hour)}:${pad(minute)}"
}

private fun pad(value: Int): String {
    return if (value < 10) "0$value" else value.toString()
}