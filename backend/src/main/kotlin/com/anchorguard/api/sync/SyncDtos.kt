package com.anchorguard.api.sync

import java.time.Instant
import java.time.LocalDate
import java.util.UUID

data class UsageBatchEventDto(
    val id: String? = null,
    val deviceUserId: UUID? = null,
    val packageName: String,
    val eventType: String,
    val startedAt: Long? = null,
    val endedAt: Long? = null,
    val durationMs: Long? = null,
    val occurredAt: Instant? = null
)

data class UsageBatchRequest(
    val deviceId: UUID? = null,
    val events: List<UsageBatchEventDto> = emptyList()
)

data class UsageBatchResponse(
    val acceptedIds: List<String> = emptyList(),
    val rejectedIds: List<String> = emptyList(),
    val message: String
)

data class SyncDeviceUserDto(
    val id: UUID,
    val deviceId: UUID,
    val childName: String,
    val ageBirthday: LocalDate? = null,
    val avatarColor: String? = null,
    val isPrimary: Boolean = false
)

data class SyncAppPolicyDto(
    val id: UUID,
    val deviceUserId: UUID?,
    val packageName: String,
    val appName: String? = null,
    val category: String? = null,
    val blocked: Boolean = false,
    val dailyLimitMinutes: Int? = null,
    val scheduleStart: String? = null,
    val scheduleEnd: String? = null,
    val weekdaysMask: Int? = null,
    val blockedMessage: String? = null
)

data class SyncDeviceSettingDto(
    val id: String,
    val deviceId: UUID,
    val key: String,
    val value: String,
    val updatedAt: Long? = null
)

data class PoliciesSyncResponse(
    val deviceId: UUID,
    val deviceUsers: List<SyncDeviceUserDto> = emptyList(),
    val appPolicies: List<SyncAppPolicyDto> = emptyList(),
    val deviceSettings: List<SyncDeviceSettingDto> = emptyList(),
    val serverTime: Long = System.currentTimeMillis()
)
