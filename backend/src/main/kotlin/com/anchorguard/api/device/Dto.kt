package com.anchorguard.api.device

import jakarta.validation.constraints.NotBlank
import java.time.Instant
import java.util.UUID

data class DeviceRegisterRequest(
    val name: String? = null,
    val model: String? = null,
    val manufacturer: String? = null,
    val androidId: String? = null,
    val appVersion: String? = null
)

data class DeviceCreateRequest(
    val name: String? = null,
    val model: String? = null,
    val manufacturer: String? = null
)

data class DeviceRegisterResponse(
    val deviceId: UUID,
    val hexId: String,
    val pairingCode: String,
    val pairingExpiresAt: Instant,
    val deviceApiKey: String
)

data class PairDeviceRequest(@field:NotBlank val pairingCode: String)

data class DeviceResponse(
    val id: UUID,
    val hexId: String,
    val name: String?,
    val model: String?,
    val manufacturer: String?,
    val appVersion: String?,
    val lastSeen: Instant?,
    val isOnline: Boolean,
    val isPaired: Boolean,
    val createdAt: Instant
)

data class DeviceDetailResponse(
    val id: UUID,
    val hexId: String,
    val name: String?,
    val model: String?,
    val manufacturer: String?,
    val androidId: String?,
    val appVersion: String?,
    val lastSeen: Instant?,
    val isOnline: Boolean,
    val isPaired: Boolean,
    val isLocked: Boolean,
    val pairingCode: String?,
    val pairingExpiresAt: Instant?,
    val createdAt: Instant
)

data class DeviceStatusResponse(
    val deviceId: UUID,
    val isLocked: Boolean,
    val lastSeen: Instant?,
    val isOnline: Boolean
)

data class LockRequest(
    val reason: String? = null
)

data class DevicePatchRequest(
    val name: String? = null
)

data class HeartbeatRequest(
    val appVersion: String? = null,
    val isOnline: Boolean? = null
)

data class SosEventRequest(
    val latitude: Double? = null,
    val longitude: Double? = null,
    val accuracyMeters: Int? = null
)

data class SosEventResponse(
    val id: UUID,
    val deviceId: UUID,
    val latitude: Double?,
    val longitude: Double?,
    val accuracyMeters: Int?,
    val occurredAt: Instant,
    val acknowledged: Boolean
)

data class SosEventListResponse(
    val deviceId: UUID,
    val events: List<SosEventResponse>
)
