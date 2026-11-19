package com.anchorguard.api.location

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.Instant
import java.util.UUID

interface LocationRepository : JpaRepository<Location, Long> {

    fun findByDeviceIdOrderByTimestampDesc(deviceId: UUID, pageable: Pageable): Page<Location>

    fun findFirstByDeviceIdOrderByTimestampDesc(deviceId: UUID): Location?

    fun findFirstByDeviceIdAndTimestampLessThanOrderByTimestampDesc(deviceId: UUID, timestamp: Instant): Location?

    @Query("SELECT l FROM Location l WHERE l.deviceId = :deviceId AND (:from IS NULL OR l.timestamp >= :from) AND (:to IS NULL OR l.timestamp <= :to) ORDER BY l.timestamp DESC")
    fun findByDeviceIdInRange(deviceId: UUID, from: Instant?, to: Instant?, pageable: Pageable): Page<Location>
}
