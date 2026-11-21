package com.anchorguard.client.ui.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anchorguard.client.auth.data.AuthRepository
import com.anchorguard.client.ui.model.Device
import kotlinx.coroutines.launch

/**
 * ViewModel for the parent dashboard.
 * Manages device list, stats, and enforcement state.
 */
class DashboardViewModel(private val authRepo: AuthRepository) : ViewModel() {

    private val _devices = mutableStateOf(listOf<Device>())
    val devices: List<Device>
        get() = _devices.value

    private val _totalDevices = mutableStateOf(0)
    val totalDevices: Int
        get() = _totalDevices.value

    private val _onlineDevices = mutableStateOf(0)
    val onlineDevices: Int
        get() = _onlineDevices.value

    private val _enforcementActive = mutableStateOf(false)
    val enforcementActive: Boolean
        get() = _enforcementActive.value

    private val _error = mutableStateOf<String?>(null)
    val error: String?
        get() = _error.value

    init {
        loadDevices()
    }

    fun loadDevices() {
        viewModelScope.launch {
            val deviceId = authRepo.deviceId()
            val isPaired = authRepo.isDevicePaired()
            val newList = if (deviceId != null && isPaired) {
                listOf(Device(
                    id = deviceId,
                    name = "Child Device",
                    model = "Android",
                    isOnline = true,
                    lastSeen = null,
                    isPaired = true,
                    enforcementActive = true
                ))
            } else {
                emptyList()
            }
            _devices.value = newList
            _totalDevices.value = newList.size
            _onlineDevices.value = newList.count { it.isOnline }
            _enforcementActive.value = true
        }
    }

    fun refresh() {
        loadDevices()
    }
}
