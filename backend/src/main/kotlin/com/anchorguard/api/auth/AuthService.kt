package com.anchorguard.api.auth

import com.anchorguard.api.common.*
import com.anchorguard.api.security.AuthPrincipal
import com.anchorguard.api.security.JwtService
import org.slf4j.LoggerFactory
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

@Service
class AuthService(
    private val parents: ParentRepository,
    private val refreshTokens: RefreshTokenRepository,
    private val resetTokens: PasswordResetTokenRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Transactional
    fun register(req: RegisterRequest): AuthTokensResponse {
        val email = req.email.lowercase().trim()
        if (parents.existsByEmail(email)) throw ConflictException("Email already registered")
        val parent = parents.save(
            Parent(
                email = email,
                passwordHash = passwordEncoder.encode(req.password),
                name = req.name?.trim()?.takeIf { it.isNotEmpty() }
            )
        )
        return issueTokens(parent)
    }

    @Transactional
    fun login(req: LoginRequest): AuthTokensResponse {
        val email = req.email.lowercase().trim()
        val parent = parents.findByEmail(email) ?: throw UnauthorizedException("Invalid credentials")
        if (!passwordEncoder.matches(req.password, parent.passwordHash)) {
            throw UnauthorizedException("Invalid credentials")
        }
        return issueTokens(parent)
    }

    @Transactional
    fun refresh(req: RefreshRequest): AuthTokensResponse {
        val claims = jwtService.parse(req.refreshToken) ?: throw UnauthorizedException("Invalid refresh token")
        if (!jwtService.isRefreshToken(claims)) throw UnauthorizedException("Invalid refresh token")
        val hash = TokenUtils.sha256(req.refreshToken)
        val stored = refreshTokens.findByTokenHash(hash) ?: throw UnauthorizedException("Refresh token not found")
        if (stored.revokedAt != null || stored.expiresAt.isBefore(Instant.now())) {
            throw UnauthorizedException("Refresh token expired or revoked")
        }
        val parentId = UUID.fromString(claims.subject)
        val parent = parents.findById(parentId).orElseThrow { UnauthorizedException("Account not found") }
        stored.revokedAt = Instant.now()
        refreshTokens.save(stored)
        return issueTokens(parent)
    }

    @Transactional
    fun logout(req: LogoutRequest) {
        val hash = TokenUtils.sha256(req.refreshToken)
        val stored = refreshTokens.findByTokenHash(hash) ?: return
        stored.revokedAt = Instant.now()
        refreshTokens.save(stored)
    }

    @Transactional
    fun requestPasswordReset(req: PasswordResetRequest): MessageResponse {
        val email = req.email.lowercase().trim()
        val parent = parents.findByEmail(email)
        if (parent != null) {
            val raw = TokenUtils.randomToken(32)
            resetTokens.save(
                PasswordResetToken(
                    parentId = parent.id,
                    tokenHash = TokenUtils.sha256(raw),
                    expiresAt = Instant.now().plus(30, ChronoUnit.MINUTES)
                )
            )
            // MVP: email delivery not configured; token logged for operator/dev use.
            log.info("Password reset requested for parent {} token={}", parent.id, raw)
        }
        return MessageResponse("If the account exists, a reset token has been issued.")
    }

    @Transactional
    fun confirmPasswordReset(req: PasswordResetConfirmRequest): MessageResponse {
        val hash = TokenUtils.sha256(req.token)
        val stored = resetTokens.findByTokenHash(hash) ?: throw BadRequestException("Invalid or expired reset token")
        if (stored.usedAt != null || stored.expiresAt.isBefore(Instant.now())) {
            throw BadRequestException("Invalid or expired reset token")
        }
        val parent = parents.findById(stored.parentId).orElseThrow { BadRequestException("Account not found") }
        parent.passwordHash = passwordEncoder.encode(req.newPassword)
        parent.updatedAt = Instant.now()
        parents.save(parent)
        stored.usedAt = Instant.now()
        resetTokens.save(stored)
        refreshTokens.deleteByParentId(parent.id)
        return MessageResponse("Password updated")
    }

    fun me(principal: AuthPrincipal): ParentResponse {
        val parent = parents.findById(principal.parentId).orElseThrow { NotFoundException("Account not found") }
        return parent.toResponse()
    }

    @Transactional
    fun changePassword(principal: AuthPrincipal, currentPassword: String, newPassword: String): MessageResponse {
        val parent = parents.findById(principal.parentId).orElseThrow { NotFoundException("Account not found") }
        if (!passwordEncoder.matches(currentPassword, parent.passwordHash)) {
            throw BadRequestException("Current password is incorrect")
        }
        parent.passwordHash = passwordEncoder.encode(newPassword)
        parent.updatedAt = Instant.now()
        parents.save(parent)
        refreshTokens.deleteByParentId(parent.id)
        return MessageResponse("Password changed")
    }

    @Transactional
    fun deleteAccount(principal: AuthPrincipal): MessageResponse {
        val parent = parents.findById(principal.parentId).orElseThrow { NotFoundException("Account not found") }
        // devices cascade policies/settings/events/users via FK in schema
        parents.delete(parent)
        return MessageResponse("Account deleted")
    }

    private fun issueTokens(parent: Parent): AuthTokensResponse {
        val access = jwtService.generateAccessToken(parent.id, parent.email, parent.roleName)
        val refresh = jwtService.generateRefreshToken(parent.id, parent.email, parent.roleName)
        refreshTokens.save(
            RefreshToken(
                parentId = parent.id,
                tokenHash = TokenUtils.sha256(refresh),
                expiresAt = jwtService.refreshExpiry()
            )
        )
        return AuthTokensResponse(accessToken = access, refreshToken = refresh, role = parent.roleName)
    }
}

fun Parent.toResponse() = ParentResponse(id, email, name, emailVerified, roleName, createdAt)
