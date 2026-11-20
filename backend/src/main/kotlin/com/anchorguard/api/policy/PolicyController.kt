package com.anchorguard.api.policy

import com.anchorguard.api.security.AuthPrincipal
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/v1/devices/{id}")
class PolicyController(private val policyService: PolicyService) {

    @GetMapping("/policy")
    fun getPolicy(@AuthenticationPrincipal principal: AuthPrincipal, @PathVariable id: UUID): DevicePolicyResponse =
        policyService.getPolicy(principal, id)

    @PutMapping("/policy")
    fun replacePolicy(
        @AuthenticationPrincipal principal: AuthPrincipal,
        @PathVariable id: UUID,
        @RequestBody req: PolicyReplaceRequest
    ): DevicePolicyResponse = policyService.replacePolicy(principal, id, req)

    @PatchMapping("/policy")
    fun patchPolicy(
        @AuthenticationPrincipal principal: AuthPrincipal,
        @PathVariable id: UUID,
        @RequestBody req: PolicyPatchRequest
    ): DevicePolicyResponse = policyService.patchPolicy(principal, id, req)

    @GetMapping("/apps")
    fun getApps(@AuthenticationPrincipal principal: AuthPrincipal, @PathVariable id: UUID): List<AppPolicyResponse> =
        policyService.listApps(principal, id)

    @PatchMapping("/apps/{packageName}")
    fun patchApp(
        @AuthenticationPrincipal principal: AuthPrincipal,
        @PathVariable id: UUID,
        @PathVariable packageName: String,
        @RequestBody req: AppPolicyPatchRequest
    ): AppPolicyResponse = policyService.patchApp(principal, id, packageName, req)

    @GetMapping("/schedule")
    fun getSchedule(@AuthenticationPrincipal principal: AuthPrincipal, @PathVariable id: UUID): ScheduleResponse =
        policyService.getSchedule(principal, id)

    @PutMapping("/schedule")
    fun setSchedule(
        @AuthenticationPrincipal principal: AuthPrincipal,
        @PathVariable id: UUID,
        @RequestBody req: ScheduleUpdateRequest
    ): ScheduleResponse = policyService.setSchedule(principal, id, req)

    @GetMapping("/allowed-categories")
    fun getAllowedCategories(@AuthenticationPrincipal principal: AuthPrincipal, @PathVariable id: UUID): List<String> =
        policyService.getAllowedCategories(principal.parentId)
}
