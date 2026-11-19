package com.anchorguard.api.report

import com.anchorguard.api.auth.MessageResponse
import com.anchorguard.api.common.UnauthorizedException
import com.anchorguard.api.device.DeviceService
import com.anchorguard.api.security.AuthPrincipal
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

@Service
class ReportService(
    private val events: UsageEventRepository,
    private val deviceService: DeviceService
) {
    @Transactional
    fun ingest(deviceId: UUID, apiKey: String?, req: UsageIngestRequest): MessageResponse {
        val device = deviceService.deviceByIdWithKey(deviceId, apiKey)
        val now = Instant.now()
        val entities = req.events.map {
            UsageEvent(
                deviceId = device.id,
                packageName = it.packageName,
                eventType = it.eventType,
                occurredAt = it.occurredAt ?: now
            )
        }
        events.saveAll(entities)
        return MessageResponse("Ingested ${entities.size} events")
    }

    fun queryAggregates(
        principal: AuthPrincipal,
        deviceId: UUID,
        from: Instant?,
        to: Instant?,
        packageName: String?
    ): List<UsageAggregateResponse> {
        deviceService.ownedDevice(principal, deviceId)
        val end = to ?: Instant.now()
        val start = from ?: end.minus(7, ChronoUnit.DAYS)
        val rows = if (packageName.isNullOrBlank()) {
            events.findAllByDeviceIdAndOccurredAtBetween(deviceId, start, end)
        } else {
            events.findAllByDeviceIdAndPackageNameAndOccurredAtBetween(deviceId, packageName, start, end)
        }
        return if (packageName.isNullOrBlank()) {
            rows.groupBy { it.packageName }.map { (pkg, items) ->
                UsageAggregateResponse(deviceId, pkg, items.size.toLong(), start, end)
            }.sortedByDescending { it.eventCount }
        } else {
            listOf(UsageAggregateResponse(deviceId, packageName, rows.size.toLong(), start, end))
        }
    }

    fun exportUsageCsv(
        principal: AuthPrincipal,
        deviceId: UUID,
        from: Instant?,
        to: Instant?
    ): String {
        deviceService.ownedDevice(principal, deviceId)
        val end = to ?: Instant.now()
        val start = from ?: end.minus(7, ChronoUnit.DAYS)
        val rows = events.findAllByDeviceIdAndOccurredAtBetween(deviceId, start, end)

        val sb = StringBuilder()
        sb.append("package_name,event_count,total_duration_ms,first_seen,last_seen\n")

        val grouped = rows.groupBy { it.packageName }
        for ((pkg, evts) in grouped.sortedByDescending { it.value.size }) {
            val totalDuration = evts.filter { it.durationMs != null }.map { it.durationMs!! }.sum()
            val firstSeen = evts.minByOrNull { it.occurredAt }?.occurredAt?.toString() ?: ""
            val lastSeen = evts.maxByOrNull { it.occurredAt }?.occurredAt?.toString() ?: ""
            sb.append("$pkg,${evts.size},$totalDuration,$firstSeen,$lastSeen\n")
        }
        return sb.toString()
    }
}
