package com.anchorguard.api.location

import com.anchorguard.api.audit.AuditLog
import com.anchorguard.api.audit.AuditLogRepository
import com.anchorguard.api.common.NotFoundException
import com.anchorguard.api.device.Device
import com.anchorguard.api.device.DeviceService
import com.anchorguard.api.security.AuthPrincipal
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension
import java.time.Instant
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class LocationServiceTest {

    private val locations = mock(LocationRepository::class.java)
    private val zones = mock(GeofenceZoneRepository::class.java)
    private val deviceService = mock(DeviceService::class.java)
    private val auditLogs = mock(AuditLogRepository::class.java)

    private val service = LocationService(locations, zones, deviceService, auditLogs)

    private val deviceId = UUID.randomUUID()
    private val apiKey = "valid-key"
    private val device = Device(id = deviceId, hexId = "abc123", apiKeyHash = "hash-of-" + apiKey)

    @Test
    fun `ingestBatch stores locations via device key auth and returns count`() {
        `when`(deviceService.deviceByIdWithKey(deviceId, apiKey)).thenReturn(device)
        `when`(locations.saveAll(any<List<Location>>())).thenAnswer { it.getArgument<List<Location>>(0) }

        val req = LocationRequest(
            deviceId = deviceId,
            locations = listOf(
                LocationRequest.LocationPoint(latitude = 37.7749, longitude = -122.4194, accuracy = 5.0, timestamp = System.currentTimeMillis()),
                LocationRequest.LocationPoint(latitude = 37.7750, longitude = -122.4195, accuracy = null, timestamp = System.currentTimeMillis() + 60000)
            )
        )

        val count = service.ingestBatch(deviceId, apiKey, req)

        assertEquals(2, count)
        verify(locations).saveAll(any<List<Location>>())
    }

    @Test
    fun `ingestBatch with non-existent device throws NotFoundException`() {
        `when`(deviceService.deviceByIdWithKey(deviceId, apiKey))
            .thenThrow(NotFoundException("Device not found"))

        val req = LocationRequest(deviceId = deviceId, locations = emptyList())

        assertThrows(NotFoundException::class.java) {
            service.ingestBatch(deviceId, apiKey, req)
        }
    }

    @Test
    fun `getLatestLocation returns most recent stored point`() {
        val expected = Location(id = 1L, deviceId = deviceId, latitude = 37.7749, longitude = -122.4194, timestamp = Instant.now())
        `when`(locations.findFirstByDeviceIdOrderByTimestampDesc(deviceId)).thenReturn(expected)

        val result = service.getLatestLocation(deviceId)

        assertNotNull(result)
        assertEquals(37.7749, result.latitude)
    }

    @Test
    fun `getLocationHistory returns paginated points ordered by timestamp desc`() {
        val page = org.springframework.data.domain.PageRequest.of(0, 20)
        val expected = listOf(
            Location(id = 1L, deviceId = deviceId, latitude = 37.7749, longitude = -122.4194, timestamp = Instant.now())
        )
        `when`(locations.findByDeviceIdInRange(deviceId, null, null, page)).thenAnswer {
            org.springframework.data.domain.PageImpl(expected, page, expected.size)
        }

        val result = service.getLocationHistory(deviceId, null, null, page)

        assertEquals(1, result.content.size)
        assertEquals(37.7749, result.content[0].latitude)
    }

    @Test
    fun `current with parent auth returns latest location response`() {
        val principal = AuthPrincipal(parentId = UUID.randomUUID(), email = "parent@anchorguard.com", role = "parent")
        `when`(deviceService.ownedDevice(principal, deviceId)).thenReturn(device)
        val expected = Location(id = 1L, deviceId = deviceId, latitude = 37.7749, longitude = -122.4194, timestamp = Instant.now())
        `when`(locations.findFirstByDeviceIdOrderByTimestampDesc(deviceId)).thenReturn(expected)

        val result = service.current(principal, deviceId)

        assertNotNull(result)
        assertEquals(37.7749, result.latitude)
        assertEquals(deviceId, result.deviceId)
    }

    @Test
    fun `current with parent auth throws NotFoundException when device not owned`() {
        val principal = AuthPrincipal(parentId = UUID.randomUUID(), email = "parent@anchorguard.com", role = "parent")
        `when`(deviceService.ownedDevice(principal, deviceId))
            .thenThrow(NotFoundException("Access denied"))

        assertThrows(NotFoundException::class.java) {
            service.current(principal, deviceId)
        }
    }

    @Test
    fun `evaluateGeofence returns true when point within radius`() {
        val zone = GeofenceZone(id = UUID.randomUUID(), deviceId = deviceId, name = "Home", latitude = 37.7749, longitude = -122.4194, radiusMeters = 100)

        assertTrue(service.evaluateGeofence(37.7749, -122.4194, zone))
    }

    @Test
    fun `evaluateGeofence returns false when point outside radius`() {
        val zone = GeofenceZone(id = UUID.randomUUID(), deviceId = deviceId, name = "Home", latitude = 37.7749, longitude = -122.4194, radiusMeters = 10)

        assertFalse(service.evaluateGeofence(37.7750, -122.4200, zone))
    }

    @Test
    fun `checkGeofenceAlerts emits ENTER when previously outside now inside`() {
        val zone = GeofenceZone(id = UUID.randomUUID(), deviceId = deviceId, name = "Home", latitude = 37.7749, longitude = -122.4194, radiusMeters = 100)
        `when`(zones.findAllByDeviceId(deviceId)).thenReturn(listOf(zone))

        val prev = Location(id = 1L, deviceId = deviceId, latitude = 37.7800, longitude = -122.4300, timestamp = Instant.now().minusSeconds(300))
        val latest = Location(id = 2L, deviceId = deviceId, latitude = 37.7749, longitude = -122.4194, timestamp = Instant.now())

        `when`(locations.findFirstByDeviceIdAndTimestampLessThanOrderByTimestampDesc(deviceId, latest.timestamp)).thenReturn(prev)

        val alerts = service.checkGeofenceAlerts(deviceId, latest)

        assertEquals(1, alerts.size)
        assertEquals("ENTER", alerts[0].alertType)
        assertEquals(zone.id, alerts[0].zoneId)
        assertEquals("Home", alerts[0].zoneName)
    }

    @Test
    fun `checkGeofenceAlerts emits EXIT when previously inside now outside`() {
        val zone = GeofenceZone(id = UUID.randomUUID(), deviceId = deviceId, name = "Home", latitude = 37.7749, longitude = -122.4194, radiusMeters = 100)
        `when`(zones.findAllByDeviceId(deviceId)).thenReturn(listOf(zone))

        val prev = Location(id = 1L, deviceId = deviceId, latitude = 37.7749, longitude = -122.4194, timestamp = Instant.now().minusSeconds(300))
        val latest = Location(id = 2L, deviceId = deviceId, latitude = 37.7800, longitude = -122.4300, timestamp = Instant.now())

        `when`(locations.findFirstByDeviceIdAndTimestampLessThanOrderByTimestampDesc(deviceId, latest.timestamp)).thenReturn(prev)

        val alerts = service.checkGeofenceAlerts(deviceId, latest)

        assertEquals(1, alerts.size)
        assertEquals("EXIT", alerts[0].alertType)
    }

    @Test
    fun `checkGeofenceAlerts with no previous point emits no alerts`() {
        val zone = GeofenceZone(id = UUID.randomUUID(), deviceId = deviceId, name = "Home", latitude = 37.7749, longitude = -122.4194, radiusMeters = 100)
        `when`(zones.findAllByDeviceId(deviceId)).thenReturn(listOf(zone))

        val latest = Location(id = 2L, deviceId = deviceId, latitude = 37.7749, longitude = -122.4194, timestamp = Instant.now())

        `when`(locations.findFirstByDeviceIdAndTimestampLessThanOrderByTimestampDesc(deviceId, latest.timestamp)).thenReturn(null)

        val alerts = service.checkGeofenceAlerts(deviceId, latest)

        assertTrue(alerts.isEmpty())
    }

    @Test
    fun `persistAlerts writes GEOFENCE_ALERT audit entries`() {
        val alert = LocationService.GeofenceAlertEvent(
            deviceId = deviceId,
            zoneId = UUID.randomUUID(),
            zoneName = "Home",
            alertType = "ENTER",
            occurredAt = Instant.now()
        )

        service.persistAlerts(listOf(alert))

        verify(auditLogs).save(any<AuditLog>())
    }
}
