package com.anchorguard.api.location

import java.time.Instant
import java.util.UUID

data class LocationResponse(
    val id: Long,
    val deviceId: UUID,
    val latitude: Double,
    val longitude: Double,
    val accuracy: Double?,
    val timestamp: Instant,
    val createdAt: Instant
)
