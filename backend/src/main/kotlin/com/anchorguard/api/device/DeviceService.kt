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
    fun heartbeat(deviceId: UUID, apiKey: String?, req: HeartbeatRequest): MessageResponse {
        val device = devices.findById(deviceId).orElseThrow { NotFoundException("Device not found") }
        verifyDeviceKey(device, apiKey)
        req.appVersion?.let { device.appVersion = it }
        device.isOnline = req.isOnline ?: true
        device.lastSeen = Instant.now()
        devices.save(device)
        return MessageResponse("ok")
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
    isPaired = parentId != null, pairingCode = pairingCode, pairingExpiresAt = pairingExpiresAt,
    createdAt = createdAt
)
