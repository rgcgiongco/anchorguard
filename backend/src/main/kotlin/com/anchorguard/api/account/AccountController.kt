package com.anchorguard.api.account

import com.anchorguard.api.auth.AuthService
import com.anchorguard.api.auth.ParentRepository
import com.anchorguard.api.auth.ParentResponse
import com.anchorguard.api.auth.toResponse
import com.anchorguard.api.auth.MessageResponse
import com.anchorguard.api.common.NotFoundException
import com.anchorguard.api.security.AuthPrincipal
import jakarta.validation.Valid
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.time.Instant

@RestController
@RequestMapping("/v1/account")
class AccountController(
    private val authService: AuthService,
    private val parents: ParentRepository
) {
    @GetMapping("/me")
    fun me(@AuthenticationPrincipal principal: AuthPrincipal): ParentResponse = authService.me(principal)

    @PatchMapping("/me")
    fun updateMe(
        @AuthenticationPrincipal principal: AuthPrincipal,
        @RequestBody req: UpdateProfileRequest
    ): ParentResponse {
        val parent = parents.findById(principal.parentId).orElseThrow { NotFoundException("Account not found") }
        req.name?.let { parent.name = it.trim().takeIf { n -> n.isNotEmpty() } }
        parent.updatedAt = Instant.now()
        return parents.save(parent).toResponse()
    }

    @PostMapping("/change-password")
    fun changePassword(
        @AuthenticationPrincipal principal: AuthPrincipal,
        @Valid @RequestBody req: ChangePasswordRequest
    ): MessageResponse = authService.changePassword(principal, req.currentPassword, req.newPassword)

    @DeleteMapping("/me")
    fun deleteMe(@AuthenticationPrincipal principal: AuthPrincipal): MessageResponse =
        authService.deleteAccount(principal)
}
