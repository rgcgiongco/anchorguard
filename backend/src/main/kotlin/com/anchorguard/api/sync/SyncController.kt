package com.anchorguard.api.sync

import com.anchorguard.api.security.AuthPrincipal
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/v1")
class SyncController(private val syncService: SyncService) {

    @PostMapping("/usage/batch")
    fun uploadUsageBatch(
        @RequestParam(value = "deviceId", required = false) deviceId: UUID?,
        @RequestHeader("X-Device-Key", required = false) apiKey: String?,
        @RequestBody req: UsageBatchRequest
    ): UsageBatchResponse {
        val resolvedDeviceId = deviceId ?: req.deviceId
            ?: throw IllegalArgumentException("deviceId is required")
        return syncService.ingestUsageBatch(resolvedDeviceId, apiKey, req)
    }

    @GetMapping("/policies")
    fun getPolicies(
        @AuthenticationPrincipal principal: AuthPrincipal?,
        @RequestParam deviceId: UUID,
        @RequestParam(required = false) since: Long?,
        @RequestHeader("X-Device-Key", required = false) apiKey: String?
    ): PoliciesSyncResponse = syncService.getPoliciesForSync(principal, deviceId, apiKey, since)
}
