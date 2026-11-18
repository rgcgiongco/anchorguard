package com.anchorguard.api.audit

import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "audit_log")
class AuditLog(
    @Id val id: UUID = UUID.randomUUID(),

    @Column(name = "device_id")
    var deviceId: UUID? = null,

    @Column(nullable = false)
    var actor: String,

    @Column(nullable = false)
    var action: String,

    @Column(name = "target_package")
    var targetPackage: String? = null,

    @Column(name = "occurred_at", nullable = false)
    var occurredAt: Instant = Instant.now(),

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "details_json", columnDefinition = "jsonb")
    var detailsJson: String? = null
)
