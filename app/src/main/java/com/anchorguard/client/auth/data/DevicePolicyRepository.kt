package com.anchorguard.client.auth.data

import android.content.Context
import com.anchorguard.client.auth.security.SecureSessionStore
import com.anchorguard.client.network.NetworkModule
import com.anchorguard.client.network.NetworkResult
import com.anchorguard.client.network.safeApiCall

class DevicePolicyRepository(context: Context) {
    private val appContext = context.applicationContext
    private val session = SecureSessionStore(appContext)

    private fun api(): DevicePolicyApi = NetworkModule.devicePolicyApi(appContext)

    suspend fun fetchAppPolicies(deviceId: String): NetworkResult<DevicePolicyResponse> {
        return safeApiCall(appContext) { api().getPolicy(deviceId) }
    }

    suspend fun fetchInstalledApps(deviceId: String): NetworkResult<List<AppPolicyResponse>> {
        return safeApiCall(appContext) { api().getApps(deviceId) }
    }

    suspend fun updateAppPolicy(
        deviceId: String,
        packageName: String,
        appName: String?,
        isAllowed: Boolean?,
        dailyLimitMinutes: Int?,
        internetAccess: String?
    ): NetworkResult<AppPolicyResponse> {
        return safeApiCall(appContext) {
            api().patchApp(
                deviceId,
                packageName,
                AppPolicyPatchRequest(
                    appName = appName,
                    isAllowed = isAllowed,
                    dailyLimitMinutes = dailyLimitMinutes,
                    blockSchedule = null,
                    internetAccess = internetAccess
                )
            )
        }
    }

    suspend fun updateDevicePolicy(
        deviceId: String,
        settings: DeviceSettingsDto?,
        apps: List<AppPolicyDto>?
    ): NetworkResult<DevicePolicyResponse> {
        return safeApiCall(appContext) {
            api().patchPolicy(deviceId, PolicyPatchRequest(settings = settings, apps = apps))
        }
    }

    suspend fun fetchSchedule(deviceId: String): NetworkResult<ScheduleResponse> {
        return safeApiCall(appContext) { api().getSchedule(deviceId) }
    }

    suspend fun updateSchedule(deviceId: String, weeklySchedule: String?): NetworkResult<ScheduleResponse> {
        return safeApiCall(appContext) {
            api().setSchedule(deviceId, ScheduleUpdateRequest(weeklySchedule = weeklySchedule))
        }
    }

    suspend fun lockDevice(deviceId: String, reason: String?): NetworkResult<MessageResponse> {
        return safeApiCall(appContext) {
            api().lockDevice(deviceId, LockRequest(reason = reason))
        }
    }

    suspend fun unlockDevice(deviceId: String): NetworkResult<MessageResponse> {
        return safeApiCall(appContext) {
            api().unlockDevice(deviceId)
        }
    }

    suspend fun getDeviceStatus(deviceId: String): NetworkResult<DeviceStatusResponse> {
        return safeApiCall(appContext) {
            api().getDeviceStatus(deviceId)
        }
    }

    suspend fun sendSos(deviceId: String, latitude: Double?, longitude: Double?, accuracyMeters: Int?): NetworkResult<SosEventResponse> {
        return safeApiCall(appContext) {
            api().sendSos(deviceId, SosEventRequest(latitude = latitude, longitude = longitude, accuracyMeters = accuracyMeters))
        }
    }

    fun deviceId(): String? = session.deviceId()
}
