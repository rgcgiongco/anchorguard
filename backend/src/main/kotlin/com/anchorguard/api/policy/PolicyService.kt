package com.anchorguard.api.policy

import com.anchorguard.api.auth.AuthService
import com.anchorguard.api.common.*
import com.anchorguard.api.device.DeviceService
import com.anchorguard.api.security.AuthPrincipal
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

@Service
class PolicyService(
    private val deviceService: DeviceService,
    private val appPolicies: AppPolicyRepository,
    private val settingsRepo: DeviceSettingsRepository,
    private val objectMapper: ObjectMapper,
    private val authService: AuthService
) {
    fun getPolicy(principal: AuthPrincipal, deviceId: UUID): DevicePolicyResponse {
        deviceService.ownedDevice(principal, deviceId)
        val settings = settingsRepo.findById(deviceId).orElse(DeviceSettings(deviceId = deviceId))
        val apps = appPolicies.findAllByDeviceId(deviceId)
        return DevicePolicyResponse(deviceId, settings.toDto(objectMapper), apps.map { it.toResponse(objectMapper) })
    }

    @Transactional
    fun replacePolicy(principal: AuthPrincipal, deviceId: UUID, req: PolicyReplaceRequest): DevicePolicyResponse {
        deviceService.ownedDevice(principal, deviceId)
        validateFreeTierCategories(principal.parentId, req.apps)
        if (req.settings != null) saveSettings(deviceId, req.settings)
        if (req.apps != null) {
            appPolicies.deleteByDeviceId(deviceId)
            req.apps.forEach { upsertApp(deviceId, it, full = true) }
        }
        return getPolicy(principal, deviceId)
    }

    @Transactional
    fun patchPolicy(principal: AuthPrincipal, deviceId: UUID, req: PolicyPatchRequest): DevicePolicyResponse {
        deviceService.ownedDevice(principal, deviceId)
        validateFreeTierCategories(principal.parentId, req.apps)
        if (req.settings != null) saveSettings(deviceId, req.settings)
        req.apps?.forEach { upsertApp(deviceId, it, full = false) }
        return getPolicy(principal, deviceId)
    }

    fun listApps(principal: AuthPrincipal, deviceId: UUID): List<AppPolicyResponse> {
        deviceService.ownedDevice(principal, deviceId)
        return appPolicies.findAllByDeviceId(deviceId).map { it.toResponse(objectMapper) }
    }

    @Transactional
    fun patchApp(principal: AuthPrincipal, deviceId: UUID, packageName: String, req: AppPolicyPatchRequest): AppPolicyResponse {
        deviceService.ownedDevice(principal, deviceId)
        val existing = appPolicies.findByDeviceIdAndPackageName(deviceId, packageName)
            ?: AppPolicy(deviceId = deviceId, packageName = packageName)
        req.appName?.let { existing.appName = it }
        req.isAllowed?.let { existing.isAllowed = it }
        if (req.dailyLimitMinutes != null || req.isAllowed != null) existing.dailyLimitMinutes = req.dailyLimitMinutes ?: existing.dailyLimitMinutes
        req.blockSchedule?.let { existing.blockSchedule = objectMapper.writeValueAsString(it) }
        req.internetAccess?.let { existing.internetAccess = it }
        existing.updatedAt = Instant.now()
        return appPolicies.save(existing).toResponse(objectMapper)
    }

    fun getSchedule(principal: AuthPrincipal, deviceId: UUID): ScheduleResponse {
        deviceService.ownedDevice(principal, deviceId)
        val settings = settingsRepo.findById(deviceId).orElse(DeviceSettings(deviceId = deviceId))
        val node = settings.weeklySchedule?.let { objectMapper.readTree(it) }
        return ScheduleResponse(deviceId, node)
    }

    @Transactional
    fun setSchedule(principal: AuthPrincipal, deviceId: UUID, req: ScheduleUpdateRequest): ScheduleResponse {
        if (!authService.isFeatureAllowed(principal.parentId, "schedules")) {
            throw ForbiddenException("Schedules require a paid subscription. Upgrade to use automated screen time limits.")
        }
        deviceService.ownedDevice(principal, deviceId)
        val settings = settingsRepo.findById(deviceId).orElse(DeviceSettings(deviceId = deviceId))
        settings.weeklySchedule = req.weeklySchedule?.let { objectMapper.writeValueAsString(it) }
        settings.updatedAt = Instant.now()
        settingsRepo.save(settings)
        return ScheduleResponse(deviceId, req.weeklySchedule)
    }

    fun getAllowedCategories(userId: UUID): List<String> {
        return authService.getAllowedCategories(userId)
    }

    private fun validateFreeTierCategories(userId: UUID, apps: List<AppPolicyDto>?) {
        if (apps == null || apps.isEmpty()) return
        val allowed = authService.getAllowedCategories(userId)
        val disallowed = apps.filter { it.category != null && it.category !in allowed }
        if (disallowed.isNotEmpty()) {
            throw ForbiddenException("Your tier only allows blocking: ${allowed.joinToString(", ")}. Upgrade to block more categories.")
        }
    }

    private fun saveSettings(deviceId: UUID, dto: DeviceSettingsDto) {
        val settings = settingsRepo.findById(deviceId).orElse(DeviceSettings(deviceId = deviceId))
        settings.advancedTimer = dto.advancedTimer
        settings.backgroundMode = dto.backgroundMode
        settings.tamperProof = dto.tamperProof
        settings.bootStart = dto.bootStart
        settings.keepAlive = dto.keepAlive
        settings.bruteForcePin = dto.bruteForcePin
        settings.blockIntervalMs = dto.blockIntervalMs
        settings.appTitle = dto.appTitle
        settings.weeklySchedule = dto.weeklySchedule?.let { objectMapper.writeValueAsString(it) }
        settings.updatedAt = Instant.now()
        settingsRepo.save(settings)
    }

    private fun upsertApp(deviceId: UUID, dto: AppPolicyDto, full: Boolean) {
        val existing = appPolicies.findByDeviceIdAndPackageName(deviceId, dto.packageName)
        val entity = existing ?: AppPolicy(deviceId = deviceId, packageName = dto.packageName)
        entity.appName = dto.appName ?: if (full) null else entity.appName
        entity.isAllowed = dto.isAllowed
        entity.dailyLimitMinutes = dto.dailyLimitMinutes
        entity.category = dto.category
        entity.blockSchedule = dto.blockSchedule?.let { objectMapper.writeValueAsString(it) }
        entity.internetAccess = dto.internetAccess
        entity.updatedAt = Instant.now()
        appPolicies.save(entity)
    }
}

fun DeviceSettings.toDto(mapper: ObjectMapper): DeviceSettingsDto = DeviceSettingsDto(
    advancedTimer, backgroundMode, tamperProof, bootStart, keepAlive, bruteForcePin, blockIntervalMs, appTitle,
    weeklySchedule = weeklySchedule?.let { mapper.readTree(it) }
)

fun AppPolicy.toResponse(mapper: ObjectMapper): AppPolicyResponse = AppPolicyResponse(
    id = id, deviceId = deviceId, packageName = packageName, appName = appName, isAllowed = isAllowed,
    dailyLimitMinutes = dailyLimitMinutes,
    blockSchedule = blockSchedule?.let { mapper.readTree(it) },
    internetAccess = internetAccess, updatedAt = updatedAt, category = category
)
