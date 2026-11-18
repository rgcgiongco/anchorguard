package com.anchorguard.api.device

import com.anchorguard.api.auth.MessageResponse
import com.anchorguard.api.security.AuthPrincipal
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/v1/devices")
class DeviceController(private val deviceService: DeviceService) {

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    fun register(@RequestBody(required = false) req: DeviceRegisterRequest?): DeviceRegisterResponse =
        deviceService.register(req ?: DeviceRegisterRequest())

    @PostMapping("/pair")
    fun pair(@AuthenticationPrincipal principal: AuthPrincipal, @Valid @RequestBody req: PairDeviceRequest): DeviceResponse =
        deviceService.pair(principal, req)

    @GetMapping
    fun list(@AuthenticationPrincipal principal: AuthPrincipal): List<DeviceResponse> =
        deviceService.list(principal)

    @GetMapping("/{id}")
    fun get(@AuthenticationPrincipal principal: AuthPrincipal, @PathVariable id: UUID): DeviceDetailResponse =
        deviceService.get(principal, id)

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
}
