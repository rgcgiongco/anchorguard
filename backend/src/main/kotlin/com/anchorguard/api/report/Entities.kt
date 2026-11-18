package com.anchorguard.api.report

import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "usage_events")
class UsageEvent(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) val id: Long? = null,

    // Legacy/device-scoped linkage retained for compatibility
    @Column(name = "device_id", nullable = false) var deviceId: UUID,
    @Column(name = "package_name", nullable = false) var packageName: String,
    @Column(name = "event_type", nullable = false) var eventType: String,
    @Column(name = "occurred_at") var occurredAt: Instant = Instant.now(),

    // Spec-aligned fields (V3+, additive)
    @Column(name = "device_user_id") var deviceUserId: UUID? = null,
    @Column(name = "started_at") var startedAt: Instant? = null,
    @Column(name = "ended_at") var endedAt: Instant? = null,
    @Column(name = "duration_ms") var durationMs: Long? = null
)
