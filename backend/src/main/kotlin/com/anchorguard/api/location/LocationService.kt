package com.anchorguard.api.location

import com.anchorguard.api.audit.AuditLog
import com.anchorguard.api.audit.AuditLogRepository
import com.anchorguard.api.device.DeviceService
import com.anchorguard.api.security.AuthPrincipal
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

@Service
class LocationService(
    private val locations: LocationRepository,
    private val zones: GeofenceZoneRepository,
    private val deviceService: DeviceService,
    private val auditLogs: AuditLogRepository
) {

    @Transactional
    fun ingestBatch(deviceId: UUID, apiKey: String?, req: LocationRequest): Int {
        val count = storeLocations(deviceId, apiKey, req)
        // Geofence evaluation against the latest stored point
        val latest = getLatestLocation(deviceId)
        if (latest != null) {
            val alerts = checkGeofenceAlerts(deviceId, latest)
            persistAlerts(alerts)
        }
        return count
    }

    @Transactional
    fun storeLocations(deviceId: UUID, apiKey: String?, req: LocationRequest): Int {
        val device = deviceService.deviceByIdWithKey(deviceId, apiKey)
        val toSave = req.locations.map { point ->
            Location(
                deviceId = device.id,
                latitude = point.latitude,
                longitude = point.longitude,
                accuracy = point.accuracy,
                timestamp = point.toInstant(),
                createdAt = Instant.now()
            )
        }
        return if (toSave.isNotEmpty()) locations.saveAll(toSave).size else 0
    }

    fun getLatestLocation(deviceId: UUID): Location? =
        locations.findFirstByDeviceIdOrderByTimestampDesc(deviceId)

    fun getLocationHistory(
        deviceId: UUID,
        fromMillis: Long?,
        toMillis: Long?,
        pageable: Pageable
    ): Page<LocationResponse> {
        val from = fromMillis?.let { Instant.ofEpochMilli(it) }
        val to = toMillis?.let { Instant.ofEpochMilli(it) }
        return locations.findByDeviceIdInRange(deviceId, from, to, pageable).map { loc ->
            LocationResponse(loc.id, loc.deviceId, loc.latitude, loc.longitude, loc.accuracy, loc.timestamp, loc.createdAt)
        }
    }

    fun history(
        principal: AuthPrincipal,
        deviceId: UUID,
        fromMillis: Long?,
        toMillis: Long?,
        pageable: Pageable
    ): Page<LocationResponse> {
        val device = deviceService.ownedDevice(principal, deviceId)
        return getLocationHistory(deviceId, fromMillis, toMillis, pageable)
    }

    fun current(principal: AuthPrincipal, deviceId: UUID): LocationResponse? {
        val device = deviceService.ownedDevice(principal, deviceId)
        return locations.findFirstByDeviceIdOrderByTimestampDesc(deviceId).map { loc ->
            LocationResponse(loc.id, loc.deviceId, loc.latitude, loc.longitude, loc.accuracy, loc.timestamp, loc.createdAt)
        }
    }

    /** Haversine great-circle distance in meters. */
    private fun haversineMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val R = 6_371_000.0
        val phi1 = Math.toRadians(lat1)
        val phi2 = Math.toRadians(lat2)
        val deltaPhi = Math.toRadians(lat2 - lat1)
        val deltaLambda = Math.toRadians(lon2 - lon1)
        val a = Math.sin(deltaPhi / 2) * Math.sin(deltaPhi / 2) +
                Math.cos(phi1) * Math.cos(phi2) * Math.sin(deltaLambda / 2) * Math.sin(deltaLambda / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1.0 - a))
        return R * c
    }

    fun evaluateGeofence(lat: Double, lon: Double, zone: GeofenceZone): Boolean =
        haversineMeters(lat, lon, zone.latitude, zone.longitude) <= zone.radiusMeters.toDouble()

    data class GeofenceAlertEvent(
        val deviceId: UUID,
        val zoneId: UUID,
        val zoneName: String,
        val alertType: String, // "ENTER" or "EXIT"
        val occurredAt: Instant
    )

    fun checkGeofenceAlerts(deviceId: UUID, latestLocation: Location): List<GeofenceAlertEvent> {
        val zoneList = zones.findAllByDeviceId(deviceId)
        val currentZones = zoneList.filter { evaluateGeofence(latestLocation.latitude, latestLocation.longitude, it) }.toMap { it.id }

        val previous = locations.findFirstByDeviceIdAndTimestampLessThanOrderByTimestampDesc(
            deviceId, latestLocation.timestamp
        )
        val previousZones = if (previous != null) {
            zoneList.filter { evaluateGeofence(previous.latitude, previous.longitude, it) }.toMap { it.id }
        } else emptyMap()

        val alerts = mutableListOf<GeofenceAlertEvent>()
        for ((zoneId, zone) in currentZones) {
            if (zoneId !in previousZones) {
                alerts += GeofenceAlertEvent(deviceId, zoneId, zone.name, "ENTER", latestLocation.timestamp)
            }
        }
        for ((zoneId, zone) in previousZones) {
            if (zoneId !in currentZones) {
                alerts += GeofenceAlertEvent(deviceId, zoneId, zone.name, "EXIT", latestLocation.timestamp)
            }
        }
        return alerts
    }

    private fun persistAlerts(alerts: List<GeofenceAlertEvent>) {
        for (alert in alerts) {
            auditLogs.save(
                AuditLog(
                    deviceId = alert.deviceId,
                    actor = "system",
                    action = "GEOFENCE_ALERT",
                    targetPackage = alert.zoneName,
                    occurredAt = alert.occurredAt,
                    detailsJson = """{"zoneId":"${alert.zoneId}","zoneName":"${alert.zoneName}","alertType":"${alert.alertType}"}"""
                )
            )
        }
    }
}
