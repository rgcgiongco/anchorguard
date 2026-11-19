package com.anchorguard.api.location

import com.anchorguard.api.common.NotFoundException
import com.anchorguard.api.device.DeviceService
import com.anchorguard.api.security.AuthPrincipal
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/v1")
class LocationController(
    private val locationService: LocationService,
    private val deviceService: DeviceService,
    private val zoneRepository: GeofenceZoneRepository
) {

    @PostMapping("/locations/batch")
    @ResponseStatus(HttpStatus.ACCEPTED)
    fun ingestBatch(
        @RequestHeader("X-Device-Key", required = false) apiKey: String?,
        @RequestBody req: LocationRequest
    ): BatchIngestResponse {
        val count = locationService.ingestBatch(req.deviceId, apiKey, req)
        return BatchIngestResponse(count)
    }

    @GetMapping("/devices/{id}/geofences")
    @ResponseStatus(HttpStatus.OK)
    fun listGeofencesDevice(
        @RequestHeader("X-Device-Key", required = false) apiKey: String?,
        @PathVariable id: UUID
    ): List<GeofenceZone> {
        val device = deviceService.deviceByIdWithKey(id, apiKey)
        return zoneRepository.findAllByDeviceId(device.id)
    }

    @GetMapping("/devices/{id}/locations")
    fun history(
        @AuthenticationPrincipal principal: AuthPrincipal,
        @PathVariable id: UUID,
        @RequestParam(required = false) from: Long?,
        @RequestParam(required = false) to: Long?,
        @PageableDefault(size = 100, sort = ["timestamp"], direction = org.springframework.data.domain.Sort.Direction.DESC) pageable: Pageable
    ): Page<LocationResponse> {
        return locationService.history(principal, id, from, to, pageable)
    }

    @GetMapping("/devices/{id}/locations/current")
    fun current(
        @AuthenticationPrincipal principal: AuthPrincipal,
        @PathVariable id: UUID
    ): LocationResponse {
        return locationService.current(principal, id)
            ?: throw NotFoundException("No location found for device $id")
    }
}

data class BatchIngestResponse(val stored: Int)
