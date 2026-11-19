package com.anchorguard.api.device

import com.anchorguard.api.auth.MessageResponse
import com.anchorguard.api.common.*
import com.anchorguard.api.security.AuthPrincipal
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.security.SecureRandom
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Locale
import java.util.UUID

@Service
class DeviceService(
    private val devices: DeviceRepository,
    private val sosEvents: SosEventRepository,
    @Value("\${anchorguard.security.pairing-code-minutes}") private val pairingMinutes: Long
) {
    private val random = SecureRandom()

    @Transactional
    fun register(req: DeviceRegisterRequest): DeviceRegisterResponse {
        val hexId = generateHexId()
        val pairingCode = TokenUtils.pairingCode(6)
        val apiKey = "agk_" + TokenUtils.randomToken(32)
        val now = Instant.now()
        val device = devices.save(
            Device(
                hexId = hexId,
                name = req.name?.trim()?.takeIf { it.isNotEmpty() },
                model = req.model,
                manufacturer = req.manufacturer,
                androidId = req.androidId,
                appVersion = req.appVersion,
                apiKeyHash = TokenUtils.sha256(apiKey),
                pairingCode = pairingCode,
                pairingExpiresAt = now.plus(pairingMinutes, ChronoUnit.MINUTES),
                isOnline = true,
                lastSeen = now
            )
        )
        return DeviceRegisterResponse(device.id, device.hexId, pairingCode, device.pairingExpiresAt!!, apiKey)
    }

    @Transactional
    fun createChild(principal: AuthPrincipal, req: DeviceCreateRequest): DeviceResponse {
        val now = Instant.now()
        val device = devices.save(
            Device(
                hexId = generateHexId(),
                name = req.name?.trim()?.takeIf { it.isNotEmpty() } ?: "Child Device",
                model = req.model,
                manufacturer = req.manufacturer,
                androidId = null,
                appVersion = null,
                apiKeyHash = null,
                pairingCode = null,
                pairingExpiresAt = null,
                parentId = principal.parentId,
                isOnline = false,
                lastSeen = now,
                createdAt = now
            )
        )
        return devices.save(device).toResponse()
    }

    @Transactional
    fun pair(principal: AuthPrincipal, req: PairDeviceRequest): DeviceResponse {
        val code = req.pairingCode.trim().uppercase(Locale.ROOT)
        val device = devices.findByPairingCode(code) ?: throw NotFoundException("Pairing code not found")
        val expires = device.pairingExpiresAt ?: throw BadRequestException("Pairing code expired")
        if (expires.isBefore(Instant.now())) throw BadRequestException("Pairing code expired")
        if (device.parentId != null) throw ConflictException("Device already paired")
        device.parentId = principal.parentId
        device.pairingCode = null
        device.pairingExpiresAt = null
        return devices.save(device).toResponse()
    }

    fun list(principal: AuthPrincipal): List<DeviceResponse> =
        devices.findAllByParentId(principal.parentId).map { it.toResponse() }

    fun get(principal: AuthPrincipal, id: UUID): DeviceDetailResponse {
        val device = ownedDevice(principal, id)
        return device.toDetailResponse()
    }

    @Transactional
    fun patch(principal: AuthPrincipal, id: UUID, req: DevicePatchRequest): DeviceDetailResponse {
        val device = ownedDevice(principal, id)
        req.name?.let { device.name = it.trim().takeIf { n -> n.isNotEmpty() } }
        return devices.save(device).toDetailResponse()
    }

    @Transactional
    fun delete(principal: AuthPrincipal, id: UUID) {
        val device = ownedDevice(principal, id)
        devices.delete(device)
    }

    @Transactional
    fun lock(principal: AuthPrincipal, id: UUID, reason: String?): MessageResponse {
        val device = ownedDevice(principal, id)
        device.isLocked = true
        devices.save(device)
        return MessageResponse("Device locked")
    }

    @Transactional
    fun unlock(principal: AuthPrincipal, id: UUID): MessageResponse {
        val device = ownedDevice(principal, id)
        device.isLocked = false
        devices.save(device)
        return MessageResponse("Device unlocked")
    }

    fun getStatus(principal: AuthPrincipal, id: UUID): DeviceStatusResponse {
        val device = ownedDevice(principal, id)
        return DeviceStatusResponse(device.id, device.isLocked, device.lastSeen, device.isOnline)
    }

    @Transactional
    fun heartbeat(deviceId: UUID, apiKey: String?, req: HeartbeatRequest): MessageResponse {
        val device = devices.findById(deviceId).orElseThrow { NotFoundException("Device not found") }
        verifyDeviceKey(device, apiKey)
        req.appVersion?.let { device.appVersion = it }
        device.isOnline = req.isOnline ?: true
        device.lastSeen = Instant.now()
        devices.save(device)
        return MessageResponse("ok")
    }

    @Transactional
    fun sos(deviceId: UUID, apiKey: String?, req: SosEventRequest): SosEventResponse {
        val device = devices.findById(deviceId).orElseThrow { NotFoundException("Device not found") }
        verifyDeviceKey(device, apiKey)
        val now = Instant.now()
        val event = sosEvents.save(
            SosEvent(
                deviceId = device.id,
                latitude = req.latitude,
                longitude = req.longitude,
                accuracyMeters = req.accuracyMeters,
                occurredAt = now
            )
        )
        return SosEventResponse(event.id, event.deviceId, event.latitude, event.longitude, event.accuracyMeters, event.occurredAt, event.acknowledged)
    }

    fun sosList(deviceId: UUID): SosEventListResponse {
        val events = sosEvents.findAllByDeviceIdOrderByOccurredAtDesc(deviceId)
        return SosEventListResponse(
            deviceId = deviceId,
            events = events.map { e ->
                SosEventResponse(e.id, e.deviceId, e.latitude, e.longitude, e.accuracyMeters, e.occurredAt, e.acknowledged)
            }
        )
    }

    @Transactional
    fun acknowledgeSos(deviceId: UUID, sosId: UUID): MessageResponse {
        val event = sosEvents.findById(sosId).orElseThrow { NotFoundException("SOS event not found") }
        if (event.deviceId != deviceId) throw ForbiddenException("Access denied")
        event.acknowledged = true
        sosEvents.save(event)
        return MessageResponse("SOS acknowledged")
    }

    fun ownedDevice(principal: AuthPrincipal, id: UUID): Device {
        val device = devices.findById(id).orElseThrow { NotFoundException("Device not found") }
        if (device.parentId != principal.parentId) throw ForbiddenException("Access denied")
        return device
    }

    fun deviceByIdWithKey(id: UUID, apiKey: String?): Device {
        val device = devices.findById(id).orElseThrow { NotFoundException("Device not found") }
        verifyDeviceKey(device, apiKey)
        return device
    }

    private fun verifyDeviceKey(device: Device, apiKey: String?) {
        val hash = device.apiKeyHash ?: return
        if (apiKey.isNullOrBlank() || TokenUtils.sha256(apiKey) != hash) {
            throw UnauthorizedException("Invalid device credentials")
        }
    }

    private fun generateHexId(): String {
        val bytes = ByteArray(8)
        random.nextBytes(bytes)
        return bytes.joinToString("") { "%02x".format(it) }
    }
}

fun Device.toResponse() = DeviceResponse(
    id = id, hexId = hexId, name = name, model = model, manufacturer = manufacturer,
    appVersion = appVersion, lastSeen = lastSeen, isOnline = isOnline,
    isPaired = parentId != null, createdAt = createdAt
)

fun Device.toDetailResponse() = DeviceDetailResponse(
    id = id, hexId = hexId, name = name, model = model, manufacturer = manufacturer,
    androidId = androidId, appVersion = appVersion, lastSeen = lastSeen, isOnline = isOnline,
    isPaired = parentId != null, isLocked = isLocked,
    pairingCode = pairingCode, pairingExpiresAt = pairingExpiresAt,
    createdAt = createdAt
)
