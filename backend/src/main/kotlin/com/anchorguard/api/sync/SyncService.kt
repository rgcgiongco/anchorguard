package com.anchorguard.api.sync

import com.anchorguard.api.device.DeviceService
import com.anchorguard.api.device.DeviceUserRepository
import com.anchorguard.api.policy.AppPolicyRepository
import com.anchorguard.api.policy.DeviceSettingsRepository
import com.anchorguard.api.report.UsageEvent
import com.anchorguard.api.report.UsageEventRepository
import com.anchorguard.api.security.AuthPrincipal
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

@Service
class SyncService(
    private val deviceService: DeviceService,
    private val usageEvents: UsageEventRepository,
    private val deviceUsers: DeviceUserRepository,
    private val appPolicies: AppPolicyRepository,
    private val settingsRepo: DeviceSettingsRepository
) {
    @Transactional
    fun ingestUsageBatch(deviceId: UUID, apiKey: String?, req: UsageBatchRequest): UsageBatchResponse {
        val device = deviceService.deviceByIdWithKey(deviceId, apiKey)
        val accepted = mutableListOf<String>()
        val rejected = mutableListOf<String>()
        val toSave = mutableListOf<UsageEvent>()
        val now = Instant.now()

        for (e in req.events) {
            try {
                if (e.packageName.isBlank() || e.eventType.isBlank()) {
                    e.id?.let { rejected.add(it) }
                    continue
                }
                val started = e.startedAt?.let { Instant.ofEpochMilli(it) } ?: e.occurredAt ?: now
                val ended = e.endedAt?.let { Instant.ofEpochMilli(it) }
                val duration = e.durationMs ?: if (ended != null) (ended.toEpochMilli() - started.toEpochMilli()).coerceAtLeast(0) else null
                toSave += UsageEvent(
                    deviceId = device.id,
                    packageName = e.packageName,
                    eventType = e.eventType,
                    occurredAt = e.occurredAt ?: started,
                    deviceUserId = e.deviceUserId,
                    startedAt = started,
                    endedAt = ended,
                    durationMs = duration
                )
                e.id?.let { accepted.add(it) }
            } catch (_: Throwable) {
                e.id?.let { rejected.add(it) }
            }
        }

        if (toSave.isNotEmpty()) usageEvents.saveAll(toSave)
        return UsageBatchResponse(
            acceptedIds = accepted,
            rejectedIds = rejected,
            message = "Ingested ${toSave.size} events"
        )
    }

    fun getPoliciesForSync(
        principal: AuthPrincipal?,
        deviceId: UUID,
        apiKey: String?,
        since: Long?
    ): PoliciesSyncResponse {
        val device = if (principal != null) {
            deviceService.ownedDevice(principal, deviceId)
        } else {
            deviceService.deviceByIdWithKey(deviceId, apiKey)
        }

        val users = deviceUsers.findAllByDeviceId(device.id).map {
            SyncDeviceUserDto(
                id = it.id,
                deviceId = it.deviceId,
                childName = it.childName ?: it.name,
                ageBirthday = it.ageBirthday,
                avatarColor = it.avatarColor,
                isPrimary = it.isPrimary
            )
        }

        val policies = appPolicies.findAllByDeviceId(device.id).map {
            SyncAppPolicyDto(
                id = it.id,
                deviceUserId = it.deviceUserId ?: users.firstOrNull()?.id,
                packageName = it.packageName,
                appName = it.appName,
                category = it.category,
                blocked = it.blocked ?: !it.isAllowed,
                dailyLimitMinutes = it.dailyLimitMinutes,
                scheduleStart = it.scheduleStart,
                scheduleEnd = it.scheduleEnd,
                weekdaysMask = it.weekdaysMask,
                blockedMessage = it.blockedMessage
            )
        }

        val settingsEntity = settingsRepo.findById(device.id).orElse(null)
        val settings = if (settingsEntity == null) {
            emptyList()
        } else {
            val base = device.id.toString()
            listOf(
                SyncDeviceSettingDto("$base:advanced_timer", device.id, "advanced_timer", settingsEntity.advancedTimer.toString()),
                SyncDeviceSettingDto("$base:background_mode", device.id, "background_mode", settingsEntity.backgroundMode.toString()),
                SyncDeviceSettingDto("$base:tamper_proof", device.id, "tamper_proof", settingsEntity.tamperProof.toString()),
                SyncDeviceSettingDto("$base:boot_start", device.id, "boot_start", settingsEntity.bootStart.toString()),
                SyncDeviceSettingDto("$base:keep_alive", device.id, "keep_alive", settingsEntity.keepAlive.toString()),
                SyncDeviceSettingDto("$base:brute_force_pin", device.id, "brute_force_pin", settingsEntity.bruteForcePin.toString()),
                SyncDeviceSettingDto("$base:block_interval_ms", device.id, "block_interval_ms", settingsEntity.blockIntervalMs.toString()),
                SyncDeviceSettingDto("$base:app_title", device.id, "app_title", settingsEntity.appTitle),
                SyncDeviceSettingDto("$base:weekly_schedule", device.id, "weekly_schedule", settingsEntity.weeklySchedule ?: "")
            )
        }

        // since is accepted for future incremental sync; current MVP returns full snapshot (cloud-wins merge client-side)
        return PoliciesSyncResponse(
            deviceId = device.id,
            deviceUsers = users,
            appPolicies = policies,
            deviceSettings = settings,
            serverTime = System.currentTimeMillis()
        )
    }
}
