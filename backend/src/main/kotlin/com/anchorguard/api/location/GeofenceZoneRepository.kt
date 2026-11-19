package com.anchorguard.api.location

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface GeofenceZoneRepository : JpaRepository<GeofenceZone, UUID> {
    fun findAllByDeviceId(deviceId: UUID): List<GeofenceZone>
}
