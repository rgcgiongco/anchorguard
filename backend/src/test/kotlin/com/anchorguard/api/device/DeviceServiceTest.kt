package com.anchorguard.api.device

import com.anchorguard.api.auth.AuthService
import com.anchorguard.api.auth.ParentRepository
import com.anchorguard.api.audit.AuditLogRepository
import com.anchorguard.api.common.BadRequestException
import com.anchorguard.api.common.ConflictException
import com.anchorguard.api.common.ForbiddenException
import com.anchorguard.api.common.NotFoundException
import com.anchorguard.api.common.TokenUtils
import com.anchorguard.api.common.UnauthorizedException
import com.anchorguard.api.security.AuthPrincipal
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import org.mockito.Mockito.mock
import org.mockito.junit.jupiter.MockitoExtension
import java.time.Instant
import java.util.UUID
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class DeviceServiceTest {

    private lateinit var service: DeviceService

    private val devices = mock(DeviceRepository::class.java)
    private val sosEvents = mock(SosEventRepository::class.java)
    private val authService = mock(AuthService::class.java)
    private val parents = mock(ParentRepository::class.java)
    private val auditLogs = mock(AuditLogRepository::class.java)

    @BeforeEach
    fun setup() {
        service = DeviceService(devices, sosEvents, authService, parents, auditLogs, 10L)
    }

    @Test
    fun `register creates device with pairing code and api key`() {
        val req = DeviceRegisterRequest(name = "Test Phone", model = "Pixel", manufacturer = "Google")
        `when`(devices.save(any())).thenAnswer { it.getArgument<Device>(0) }

        val result = service.register(req)

        assertNotNull(result.deviceId)
        assertNotNull(result.hexId)
        assertEquals(6, result.pairingCode.length)
        assertTrue(result.deviceApiKey.startsWith("agk_"))
        assertNotNull(result.pairingExpiresAt)
        assertTrue(result.pairingExpiresAt.isAfter(Instant.now()))
        verify(devices).save(any())
    }

    @Test
    fun `pair with valid code binds device to parent`() {
        val parentId = UUID.randomUUID()
        val principal = AuthPrincipal(parentId, "test@anchorguard.com")
        val device = Device(
            id = UUID.randomUUID(),
            hexId = "abc123",
            pairingCode = "ABC123",
            pairingExpiresAt = Instant.now().plusSeconds(600)
        )
        `when`(devices.findByPairingCode("ABC123")).thenReturn(device)
        `when`(devices.save(any())).thenAnswer { it.getArgument<Device>(0) }

        val result = service.pair(principal, PairDeviceRequest("ABC123"))

        assertTrue(result.isPaired)
        verify(devices).save(any())
    }

    @Test
    fun `pair with unknown code throws NotFoundException`() {
        `when`(devices.findByPairingCode("INVALID")).thenReturn(null)

        assertThrows(NotFoundException::class.java) {
            service.pair(AuthPrincipal(UUID.randomUUID(), "test@anchorguard.com"), PairDeviceRequest("INVALID"))
        }
    }

    @Test
    fun `pair with expired code throws BadRequestException`() {
        val principal = AuthPrincipal(UUID.randomUUID(), "test@anchorguard.com")
        val device = Device(
            id = UUID.randomUUID(),
            hexId = "abc123",
            pairingCode = "EXPIRED",
            pairingExpiresAt = Instant.now().minusSeconds(60)
        )
        `when`(devices.findByPairingCode("EXPIRED")).thenReturn(device)

        assertThrows(BadRequestException::class.java) {
            service.pair(principal, PairDeviceRequest("EXPIRED"))
        }
    }

    @Test
    fun `pair with already-paired device throws ConflictException`() {
        val principal = AuthPrincipal(UUID.randomUUID(), "test@anchorguard.com")
        val otherParentId = UUID.randomUUID()
        val device = Device(
            id = UUID.randomUUID(),
            hexId = "abc123",
            pairingCode = "PAIRED",
            pairingExpiresAt = Instant.now().plusSeconds(600),
            parentId = otherParentId
        )
        `when`(devices.findByPairingCode("PAIRED")).thenReturn(device)

        assertThrows(ConflictException::class.java) {
            service.pair(principal, PairDeviceRequest("PAIRED"))
        }
    }

    @Test
    fun `heartbeat with valid device key updates lastSeen`() {
        val deviceId = UUID.randomUUID()
        val apiKey = "valid-key"
        val hash = TokenUtils.sha256(apiKey)
        val device = Device(
            id = deviceId,
            hexId = "abc123",
            apiKeyHash = hash,
            lastSeen = Instant.now().minusSeconds(300)
        )
        `when`(devices.findById(deviceId)).thenReturn(Optional.of(device))
        `when`(devices.save(any())).thenAnswer { it.getArgument<Device>(0) }

        val result = service.heartbeat(deviceId, apiKey, HeartbeatRequest(appVersion = "1.0.0"))

        assertEquals("ok", result.message)
        assertNotNull(device.lastSeen)
        assertTrue(device.lastSeen!!.isAfter(Instant.now().minusSeconds(10)))
        assertEquals("1.0.0", device.appVersion)
        verify(devices).save(any())
    }

    @Test
    fun `heartbeat with invalid device key throws UnauthorizedException`() {
        val deviceId = UUID.randomUUID()
        val device = Device(
            id = deviceId,
            hexId = "abc123",
            apiKeyHash = "some-hash"
        )
        `when`(devices.findById(deviceId)).thenReturn(Optional.of(device))

        assertThrows(UnauthorizedException::class.java) {
            service.heartbeat(deviceId, "wrong-key", HeartbeatRequest())
        }
    }

    @Test
    fun `ownedDevice by non-owner throws ForbiddenException`() {
        val ownerId = UUID.randomUUID()
        val requesterId = UUID.randomUUID()
        val device = Device(
            id = UUID.randomUUID(),
            hexId = "abc123",
            parentId = ownerId
        )
        `when`(devices.findById(device.id)).thenReturn(Optional.of(device))

        assertThrows(ForbiddenException::class.java) {
            service.ownedDevice(AuthPrincipal(requesterId, "test@anchorguard.com"), device.id)
        }
    }

    @Test
    fun `deviceByIdWithKey with missing key throws UnauthorizedException`() {
        val deviceId = UUID.randomUUID()
        val device = Device(
            id = deviceId,
            hexId = "abc123",
            apiKeyHash = "some-hash"
        )
        `when`(devices.findById(deviceId)).thenReturn(Optional.of(device))

        assertThrows(UnauthorizedException::class.java) {
            service.deviceByIdWithKey(deviceId, null)
        }
    }
}
