package com.anchorguard.api.report

import org.springframework.data.jpa.repository.JpaRepository
import java.time.Instant
import java.util.UUID

interface UsageEventRepository : JpaRepository<UsageEvent, Long> {
    fun findAllByDeviceIdAndOccurredAtBetween(deviceId: UUID, from: Instant, to: Instant): List<UsageEvent>
    fun findAllByDeviceIdAndPackageNameAndOccurredAtBetween(
        deviceId: UUID, packageName: String, from: Instant, to: Instant
    ): List<UsageEvent>
}
