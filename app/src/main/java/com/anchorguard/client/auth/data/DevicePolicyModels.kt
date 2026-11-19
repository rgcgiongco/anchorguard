package com.anchorguard.client.auth.data

data class DevicePolicyResponse(
    val deviceId: String,
    val settings: DeviceSettingsDto,
    val apps: List<AppPolicyResponse> = emptyList()
)

data class DeviceSettingsDto(
    val advancedTimer: Boolean = false,
    val backgroundMode: Boolean = false,
    val tamperProof: Boolean = false,
    val bootStart: Boolean = false,
    val keepAlive: Boolean = false,
    val bruteForcePin: Boolean = false,
    val blockIntervalMs: Int = 500,
    val appTitle: String = "AnchorGuard",
    val weeklySchedule: String? = null
)

data class AppPolicyResponse(
    val id: String,
    val deviceId: String,
    val packageName: String,
    val appName: String?,
    val isAllowed: Boolean,
    val dailyLimitMinutes: Int?,
    val blockSchedule: String?,
    val internetAccess: String,
    val updatedAt: String
)

data class AppPolicyPatchRequest(
    val appName: String? = null,
    val isAllowed: Boolean? = null,
    val dailyLimitMinutes: Int? = null,
    val blockSchedule: String? = null,
    val internetAccess: String? = null
)

data class PolicyPatchRequest(
    val settings: DeviceSettingsDto? = null,
    val apps: List<AppPolicyDto>? = null
)

data class AppPolicyDto(
    val packageName: String,
    val appName: String? = null,
    val isAllowed: Boolean = true,
    val dailyLimitMinutes: Int? = null,
    val blockSchedule: String? = null,
    val internetAccess: String = "on"
)

data class ScheduleResponse(
    val deviceId: String,
    val weeklySchedule: String? = null
)

data class ScheduleUpdateRequest(
    val weeklySchedule: String? = null
)

data class LockRequest(
    val reason: String? = null
)

data class DeviceStatusResponse(
    val deviceId: String,
    val isLocked: Boolean,
    val lastSeen: String?,
    val isOnline: Boolean
)

data class DeviceResponse(
    val id: String,
    val hexId: String,
    val name: String?,
    val model: String?,
    val manufacturer: String?,
    val appVersion: String?,
    val lastSeen: String?,
    val isOnline: Boolean,
    val isPaired: Boolean,
    val createdAt: String?
)

data class DeviceDetailResponse(
    val id: String,
    val hexId: String,
    val name: String?,
    val model: String?,
    val manufacturer: String?,
    val androidId: String?,
    val appVersion: String?,
    val lastSeen: String?,
    val isOnline: Boolean,
    val isPaired: Boolean,
    val isLocked: Boolean,
    val pairingCode: String?,
    val pairingExpiresAt: String?,
    val createdAt: String?
)

data class DeviceCreateRequest(
    val name: String? = null,
    val model: String? = null,
    val manufacturer: String? = null
)

data class DevicePatchRequest(
    val name: String? = null
)

data class SosEventRequest(
    val latitude: Double? = null,
    val longitude: Double? = null,
    val accuracyMeters: Int? = null
)

data class SosEventResponse(
    val id: String,
    val deviceId: String,
    val latitude: Double?,
    val longitude: Double?,
    val accuracyMeters: Int?,
    val occurredAt: String?,
    val acknowledged: Boolean
)
