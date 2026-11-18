package com.anchorguard.api.report

import com.anchorguard.api.auth.MessageResponse
import com.anchorguard.api.security.AuthPrincipal
import jakarta.validation.Valid
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.time.Instant
import java.util.UUID

@RestController
@RequestMapping("/v1/reports")
class ReportController(private val reportService: ReportService) {

    @PostMapping("/usage")
    fun ingest(
        @RequestParam("deviceId") deviceId: UUID,
        @RequestHeader("X-Device-Key", required = false) apiKey: String?,
        @Valid @RequestBody req: UsageIngestRequest
    ): MessageResponse = reportService.ingest(deviceId, apiKey, req)

    @GetMapping("/usage")
    fun usage(
        @AuthenticationPrincipal principal: AuthPrincipal,
        @RequestParam deviceId: UUID,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) from: Instant?,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) to: Instant?,
        @RequestParam(required = false) packageName: String?
    ): List<UsageAggregateResponse> = reportService.queryAggregates(principal, deviceId, from, to, packageName)

    @GetMapping("/export")
    fun export(): ResponseEntity<MessageResponse> =
        ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(MessageResponse("CSV export deferred"))
}
