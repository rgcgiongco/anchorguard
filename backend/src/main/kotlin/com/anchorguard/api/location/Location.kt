package com.anchorguard.api.location

import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "locations")
class Location(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,

    @Column(name = "device_id", nullable = false)
    var deviceId: UUID = UUID.randomUUID(),

    @Column(name = "latitude", nullable = false)
    var latitude: Double = 0.0,

    @Column(name = "longitude", nullable = false)
    var longitude: Double = 0.0,

    @Column(name = "accuracy")
    var accuracy: Double? = null,

    @Column(name = "timestamp", nullable = false)
    var timestamp: Instant = Instant.now(),

    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: Instant = Instant.now()
)
