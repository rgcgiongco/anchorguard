package com.anchorguard.api.auth

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.Instant
import java.util.UUID

data class RegisterRequest(
    @field:Email @field:NotBlank val email: String,
    @field:NotBlank @field:Size(min = 8, max = 128) val password: String,
    val name: String? = null
)

data class LoginRequest(
    @field:Email @field:NotBlank val email: String,
    @field:NotBlank val password: String
)

data class RefreshRequest(@field:NotBlank val refreshToken: String)
data class LogoutRequest(@field:NotBlank val refreshToken: String)

data class PasswordResetRequest(@field:Email @field:NotBlank val email: String)
data class PasswordResetConfirmRequest(
    @field:NotBlank val token: String,
    @field:NotBlank @field:Size(min = 8, max = 128) val newPassword: String
)

data class AuthTokensResponse(val accessToken: String, val refreshToken: String, val tokenType: String = "Bearer")
data class MessageResponse(val message: String)
data class ParentResponse(val id: UUID, val email: String, val name: String?, val emailVerified: Boolean, val createdAt: Instant)
