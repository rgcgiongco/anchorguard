package com.anchorguard.api.location

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "geofence_zones")
class GeofenceZone(
    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(name = "device_id", nullable = false)
    var deviceId: UUID,

    @Column(nullable = false)
    var name: String = "",

    @Column(name = "latitude", nullable = false)
    var latitude: Double = 0.0,

    @Column(name = "longitude", nullable = false)
    var longitude: Double = 0.0,

    @Column(name = "radius_meters", nullable = false)
    var radiusMeters: Int = 0,

    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: Instant = Instant.now()
)
