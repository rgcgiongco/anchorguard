package com.anchorguard.client.auth.data

import android.content.Context
import com.anchorguard.client.network.NetworkModule
import com.anchorguard.client.network.NetworkResult
import com.anchorguard.client.network.safeApiCall

class DeviceRepository(context: Context) {
    private val appContext = context.applicationContext

    private fun api(): DeviceManagementApi = NetworkModule.deviceManagementApi(appContext)

    suspend fun listDevices(): NetworkResult<List<DeviceResponse>> {
        return safeApiCall(appContext) { api().listDevices() }
    }

    suspend fun createChildDevice(name: String?, model: String?, manufacturer: String?): NetworkResult<DeviceResponse> {
        return safeApiCall(appContext) {
            api().createChildDevice(DeviceCreateRequest(name = name, model = model, manufacturer = manufacturer))
        }
    }

    suspend fun renameDevice(deviceId: String, name: String): NetworkResult<DeviceDetailResponse> {
        return safeApiCall(appContext) {
            api().renameDevice(deviceId, DevicePatchRequest(name = name))
        }
    }

    suspend fun removeDevice(deviceId: String): NetworkResult<MessageResponse> {
        return safeApiCall(appContext) { api().removeDevice(deviceId) }
    }
}
