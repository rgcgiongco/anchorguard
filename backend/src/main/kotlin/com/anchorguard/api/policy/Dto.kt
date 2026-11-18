package com.anchorguard.api.policy

import com.fasterxml.jackson.databind.JsonNode
import java.time.Instant
import java.util.UUID

data class AppPolicyDto(
    val packageName: String,
    val appName: String? = null,
    val isAllowed: Boolean = true,
    val dailyLimitMinutes: Int? = null,
    val blockSchedule: JsonNode? = null,
    val internetAccess: String = "on"
)

data class AppPolicyResponse(
    val id: UUID,
    val deviceId: UUID,
    val packageName: String,
    val appName: String?,
    val isAllowed: Boolean,
    val dailyLimitMinutes: Int?,
    val blockSchedule: JsonNode?,
    val internetAccess: String,
    val updatedAt: Instant
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
    val weeklySchedule: JsonNode? = null
)

data class DevicePolicyResponse(
    val deviceId: UUID,
    val settings: DeviceSettingsDto,
    val apps: List<AppPolicyResponse>
)

data class PolicyReplaceRequest(
    val settings: DeviceSettingsDto? = null,
    val apps: List<AppPolicyDto>? = null
)

data class PolicyPatchRequest(
    val settings: DeviceSettingsDto? = null,
    val apps: List<AppPolicyDto>? = null
)

data class AppPolicyPatchRequest(
    val appName: String? = null,
    val isAllowed: Boolean? = null,
    val dailyLimitMinutes: Int? = null,
    val blockSchedule: JsonNode? = null,
    val internetAccess: String? = null
)

data class ScheduleResponse(val deviceId: UUID, val weeklySchedule: JsonNode?)
data class ScheduleUpdateRequest(val weeklySchedule: JsonNode?)
