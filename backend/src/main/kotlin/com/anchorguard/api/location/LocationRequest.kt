package com.anchorguard.api.location

import java.time.Instant
import java.util.UUID

data class LocationRequest(
    val deviceId: UUID,
    val locations: List<LocationPoint>
) {
    data class LocationPoint(
        val latitude: Double,
        val longitude: Double,
        val accuracy: Double? = null,
        val timestamp: Long
    ) {
        fun toInstant() = Instant.ofEpochMilli(timestamp)
    }
}
