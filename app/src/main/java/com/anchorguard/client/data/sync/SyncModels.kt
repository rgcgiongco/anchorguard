package com.anchorguard.client.data.sync

import com.google.gson.annotations.SerializedName

data class UsageBatchEvent(
    val id: String,
    val deviceUserId: String,
    val packageName: String,
    val eventType: String,
    val startedAt: Long,
    val endedAt: Long? = null,
    val durationMs: Long? = null
)

data class UsageBatchRequest(
    val deviceId: String? = null,
    val events: List<UsageBatchEvent>
)

data class UsageBatchResponse(
    val acceptedIds: List<String> = emptyList(),
    val rejectedIds: List<String> = emptyList(),
    val message: String? = null
)

data class RemoteDeviceUser(
    val id: String = "",
    val deviceId: String = "",
    val childName: String = "",
    val ageBirthday: String? = null,
    val avatarColor: String? = null,
    val isPrimary: Boolean = false
)

data class RemoteAppPolicy(
    val id: String = "",
    val deviceUserId: String? = null,
    val packageName: String = "",
    val appName: String? = null,
    val category: String? = null,
    val blocked: Boolean = false,
    val dailyLimitMinutes: Int? = null,
    val scheduleStart: String? = null,
    val scheduleEnd: String? = null,
    val weekdaysMask: Int? = null,
    val blockedMessage: String? = null
)

data class RemoteDeviceSetting(
    val id: String? = null,
    val deviceId: String = "",
    val key: String = "",
    val value: String = "",
    val updatedAt: Long? = null
)

data class PoliciesResponse(
    val deviceId: String? = null,
    val deviceUsers: List<RemoteDeviceUser> = emptyList(),
    val appPolicies: List<RemoteAppPolicy> = emptyList(),
    val deviceSettings: List<RemoteDeviceSetting> = emptyList(),
    @SerializedName("serverTime")
    val serverTime: Long? = null
)

data class LocationBatchRequest(
    val deviceId: String,
    val locations: List<LocationPoint>
)

data class LocationPoint(
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float,
    val timestamp: Long
)

data class LocationBatchResponse(
    val acceptedIds: List<String> = emptyList(),
    val rejectedIds: List<String> = emptyList(),
    val message: String? = null
)

data class GeofenceZoneDto(
    val id: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val radiusMeters: Int
)
