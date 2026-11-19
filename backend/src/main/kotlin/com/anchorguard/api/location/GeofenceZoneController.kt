package com.anchorguard.api.location

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import com.anchorguard.api.security.AuthPrincipal
import java.time.Instant
import java.util.UUID

data class CreateGeofenceZoneRequest(
    @field:NotBlank val name: String,
    @field:NotBlank val latitude: Double,
    @field:NotBlank val longitude: Double,
    @field:Positive val radiusMeters: Int
)

@RestController
@RequestMapping("/v1/devices")
class GeofenceZoneController(
    private val deviceService: com.anchorguard.api.device.DeviceService,
    private val zoneRepository: GeofenceZoneRepository,
    private val locationService: LocationService
) {

    @GetMapping("/{id}/geofences")
    fun listZones(
        @AuthenticationPrincipal principal: AuthPrincipal,
        @PathVariable id: UUID
    ): List<GeofenceZone> {
        deviceService.ownedDevice(principal, id)
        return zoneRepository.findAllByDeviceId(id)
    }

    @PostMapping("/{id}/geofences")
    @ResponseStatus(HttpStatus.CREATED)
    fun createZone(
        @AuthenticationPrincipal principal: AuthPrincipal,
        @PathVariable id: UUID,
        @RequestBody req: CreateGeofenceZoneRequest
    ): GeofenceZone {
        deviceService.ownedDevice(principal, id)
        val zone = GeofenceZone(
            deviceId = id,
            name = req.name.trim(),
            latitude = req.latitude,
            longitude = req.longitude,
            radiusMeters = req.radiusMeters,
            createdAt = Instant.now()
        )
        return zoneRepository.save(zone)
    }

    @DeleteMapping("/geofences/{zoneId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteZone(
        @AuthenticationPrincipal principal: AuthPrincipal,
        @PathVariable zoneId: UUID
    ) {
        val zone = zoneRepository.findById(zoneId)
            .orElseThrow { com.anchorguard.api.common.NotFoundException("Geofence zone not found") }
        deviceService.ownedDevice(principal, zone.deviceId)
        zoneRepository.delete(zone)
    }
}
