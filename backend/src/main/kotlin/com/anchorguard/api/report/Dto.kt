package com.anchorguard.api.report

import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import java.time.Instant
import java.util.UUID

data class UsageEventIngestItem(
    @field:NotBlank val packageName: String,
    @field:NotBlank val eventType: String,
    val occurredAt: Instant? = null
)

data class UsageIngestRequest(
    @field:NotEmpty val events: List<@Valid UsageEventIngestItem>
)

data class UsageEventResponse(
    val id: Long?,
    val deviceId: UUID,
    val packageName: String,
    val eventType: String,
    val occurredAt: Instant
)

data class UsageAggregateResponse(
    val deviceId: UUID,
    val packageName: String?,
    val eventCount: Long,
    val from: Instant,
    val to: Instant
)
