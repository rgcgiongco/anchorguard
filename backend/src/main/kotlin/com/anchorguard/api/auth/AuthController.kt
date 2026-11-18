package com.anchorguard.api.auth

import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/v1/auth")
class AuthController(private val authService: AuthService) {
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    fun register(@Valid @RequestBody req: RegisterRequest): AuthTokensResponse = authService.register(req)

    @PostMapping("/login")
    fun login(@Valid @RequestBody req: LoginRequest): AuthTokensResponse = authService.login(req)

    @PostMapping("/refresh")
    fun refresh(@Valid @RequestBody req: RefreshRequest): AuthTokensResponse = authService.refresh(req)

    @PostMapping("/logout")
    fun logout(@Valid @RequestBody req: LogoutRequest): MessageResponse {
        authService.logout(req)
        return MessageResponse("Logged out")
    }

    @PostMapping("/password-reset/request")
    fun requestReset(@Valid @RequestBody req: PasswordResetRequest): MessageResponse =
        authService.requestPasswordReset(req)

    @PostMapping("/password-reset/confirm")
    fun confirmReset(@Valid @RequestBody req: PasswordResetConfirmRequest): MessageResponse =
        authService.confirmPasswordReset(req)
}
