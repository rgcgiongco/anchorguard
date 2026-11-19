package com.anchorguard.api.device

import com.anchorguard.api.auth.MessageResponse
import com.anchorguard.api.report.ReportService
import com.anchorguard.api.security.AuthPrincipal
import jakarta.validation.Valid
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.nio.charset.Charsets
import java.time.Instant
import java.util.UUID

@RestController
@RequestMapping("/v1/devices")
class DeviceController(
    private val deviceService: DeviceService,
    private val reportService: ReportService
) {

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    fun register(@RequestBody(required = false) req: DeviceRegisterRequest?): DeviceRegisterResponse =
        deviceService.register(req ?: DeviceRegisterRequest())

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createChild(
        @AuthenticationPrincipal principal: AuthPrincipal,
        @Valid @RequestBody req: DeviceCreateRequest
    ): DeviceResponse = deviceService.createChild(principal, req)

    @PostMapping("/pair")
    fun pair(@AuthenticationPrincipal principal: AuthPrincipal, @Valid @RequestBody req: PairDeviceRequest): DeviceResponse =
        deviceService.pair(principal, req)

    @GetMapping
    fun list(@AuthenticationPrincipal principal: AuthPrincipal): List<DeviceResponse> =
        deviceService.list(principal)

    @GetMapping("/{id}")
    fun get(@AuthenticationPrincipal principal: AuthPrincipal, @PathVariable id: UUID): DeviceDetailResponse =
        deviceService.get(principal, id)

    @GetMapping("/{id}/status")
    fun getStatus(@AuthenticationPrincipal principal: AuthPrincipal, @PathVariable id: UUID): DeviceStatusResponse =
        deviceService.getStatus(principal, id)

    @PatchMapping("/{id}")
    fun patch(
        @AuthenticationPrincipal principal: AuthPrincipal,
        @PathVariable id: UUID,
        @RequestBody req: DevicePatchRequest
    ): DeviceDetailResponse = deviceService.patch(principal, id, req)

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(@AuthenticationPrincipal principal: AuthPrincipal, @PathVariable id: UUID) =
        deviceService.delete(principal, id)

    @PostMapping("/{id}/heartbeat")
    fun heartbeat(
        @PathVariable id: UUID,
        @RequestHeader("X-Device-Key", required = false) apiKey: String?,
        @RequestBody(required = false) req: HeartbeatRequest?
    ): MessageResponse = deviceService.heartbeat(id, apiKey, req ?: HeartbeatRequest())

    @PostMapping("/{id}/sos")
    fun sos(
        @PathVariable id: UUID,
        @RequestHeader("X-Device-Key", required = false) apiKey: String?,
        @RequestBody(required = false) req: SosEventRequest?
    ): SosEventResponse = deviceService.sos(id, apiKey, req ?: SosEventRequest())

    @GetMapping("/{id}/sos")
    fun sosList(
        @AuthenticationPrincipal principal: AuthPrincipal,
        @PathVariable id: UUID
    ): SosEventListResponse = deviceService.sosList(id)

    @PostMapping("/{id}/sos/{sosId}/acknowledge")
    fun acknowledgeSos(
        @AuthenticationPrincipal principal: AuthPrincipal,
        @PathVariable id: UUID,
        @PathVariable sosId: UUID
    ): MessageResponse = deviceService.acknowledgeSos(id, sosId)

    @PostMapping("/{id}/lock")
    fun lock(
        @AuthenticationPrincipal principal: AuthPrincipal,
        @PathVariable id: UUID,
        @RequestBody(required = false) req: LockRequest?
    ): MessageResponse = deviceService.lock(principal, id, req?.reason)

    @PostMapping("/{id}/unlock")
    fun unlock(
        @AuthenticationPrincipal principal: AuthPrincipal,
        @PathVariable id: UUID
    ): MessageResponse = deviceService.unlock(principal, id)

    @GetMapping("/{id}/usage/export")
    fun exportUsage(
        @AuthenticationPrincipal principal: AuthPrincipal,
        @PathVariable id: UUID,
        @RequestParam(required = false) from: Instant?,
        @RequestParam(required = false) to: Instant?
    ): ResponseEntity<byte[]> {
        val csv = reportService.exportUsageCsv(principal, id, from, to)
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"usage-export-$id.csv\"")
            .contentType(MediaType.parseMediaType("text/csv; charset=utf-8"))
            .body(csv.toByteArray(Charsets.UTF_8))
    }
}
