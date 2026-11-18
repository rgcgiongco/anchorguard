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
    val pairingCode: String?,
    val pairingExpiresAt: Instant?,
    val createdAt: Instant
)

data class DevicePatchRequest(
    val name: String? = null
)

data class HeartbeatRequest(
    val appVersion: String? = null,
    val isOnline: Boolean? = null
)
