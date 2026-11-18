package com.anchorguard.api.auth

import com.anchorguard.api.common.BadRequestException
import com.anchorguard.api.common.ConflictException
import com.anchorguard.api.common.NotFoundException
import com.anchorguard.api.common.TokenUtils
import com.anchorguard.api.common.UnauthorizedException
import com.anchorguard.api.security.JwtService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.ArgumentMatchers.eq
import org.mockito.ArgumentMatchers.isA
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import org.mockito.Mockito.mock
import org.springframework.security.crypto.password.PasswordEncoder
import java.time.Instant
import java.util.UUID

class AuthServiceTest {

    private val jwtSecret = "test-secret-key-123456789012345678901234567890123456789012345678901234"

    @Test
    fun `register rejects duplicate email`() {
        val parents = mock(ParentRepository::class.java)
        val refreshTokens = mock(RefreshTokenRepository::class.java)
        val resetTokens = mock(PasswordResetTokenRepository::class.java)
        val passwordEncoder = mock(PasswordEncoder::class.java)
        val jwtService = JwtService(jwtSecret, 30L, 7L)
        val service = AuthService(parents, refreshTokens, resetTokens, passwordEncoder, jwtService)

        val req = RegisterRequest("alice@anchorguard.com", "Password123!", "Alice")
        `when`(parents.existsByEmail("alice@anchorguard.com")).thenReturn(true)

        assertThrows(ConflictException::class.java) { service.register(req) }
    }

    @Test
    fun `register creates parent and issues tokens`() {
        val parents = mock(ParentRepository::class.java)
        val refreshTokens = mock(RefreshTokenRepository::class.java)
        val resetTokens = mock(PasswordResetTokenRepository::class.java)
        val passwordEncoder = mock(PasswordEncoder::class.java)
        val jwtService = JwtService(jwtSecret, 30L, 7L)
        val service = AuthService(parents, refreshTokens, resetTokens, passwordEncoder, jwtService)

        val req = RegisterRequest("alice@anchorguard.com", "Password123!", "Alice")
        `when`(parents.existsByEmail("alice@anchorguard.com")).thenReturn(false)
        `when`(passwordEncoder.encode("Password123!")).thenReturn("hashed-password")
        `when`(parents.save(isA(Parent::class.java))).thenAnswer { it.getArgument<Parent>(0) }

        val result = service.register(req)

        assertNotNull(result.accessToken)
        assertNotNull(result.refreshToken)
        assertTrue(result.accessToken.isNotEmpty())
        assertTrue(result.refreshToken.isNotEmpty())
        verify(parents).save(org.mockito.ArgumentMatchers.any())
        verify(refreshTokens).save(org.mockito.ArgumentMatchers.any())
    }

    @Test
    fun `login with valid credentials returns tokens`() {
        val parents = mock(ParentRepository::class.java)
        val refreshTokens = mock(RefreshTokenRepository::class.java)
        val resetTokens = mock(PasswordResetTokenRepository::class.java)
        val passwordEncoder = mock(PasswordEncoder::class.java)
        val jwtService = JwtService(jwtSecret, 30L, 7L)
        val service = AuthService(parents, refreshTokens, resetTokens, passwordEncoder, jwtService)

        val parent = Parent(
            id = UUID.randomUUID(),
            email = "alice@anchorguard.com",
            passwordHash = "hashed",
            name = "Alice"
        )
        val req = LoginRequest("alice@anchorguard.com", "Password123!")
        `when`(parents.findByEmail("alice@anchorguard.com")).thenReturn(parent)
        `when`(passwordEncoder.matches("Password123!", "hashed")).thenReturn(true)

        val result = service.login(req)

        assertNotNull(result.accessToken)
        assertNotNull(result.refreshToken)
        assertTrue(result.accessToken.isNotEmpty())
        assertTrue(result.refreshToken.isNotEmpty())
    }

    @Test
    fun `login with wrong password throws UnauthorizedException`() {
        val parents = mock(ParentRepository::class.java)
        val refreshTokens = mock(RefreshTokenRepository::class.java)
        val resetTokens = mock(PasswordResetTokenRepository::class.java)
        val passwordEncoder = mock(PasswordEncoder::class.java)
        val jwtService = JwtService(jwtSecret, 30L, 7L)
        val service = AuthService(parents, refreshTokens, resetTokens, passwordEncoder, jwtService)

        val parent = Parent(id = UUID.randomUUID(), email = "alice@anchorguard.com", passwordHash = "hashed")
        val req = LoginRequest("alice@anchorguard.com", "WrongPassword!")
        `when`(parents.findByEmail("alice@anchorguard.com")).thenReturn(parent)
        `when`(passwordEncoder.matches("WrongPassword!", "hashed")).thenReturn(false)

        assertThrows(UnauthorizedException::class.java) { service.login(req) }
    }

    @Test
    fun `login with unknown email throws UnauthorizedException`() {
        val parents = mock(ParentRepository::class.java)
        val refreshTokens = mock(RefreshTokenRepository::class.java)
        val resetTokens = mock(PasswordResetTokenRepository::class.java)
        val passwordEncoder = mock(PasswordEncoder::class.java)
        val jwtService = JwtService(jwtSecret, 30L, 7L)
        val service = AuthService(parents, refreshTokens, resetTokens, passwordEncoder, jwtService)

        `when`(parents.findByEmail("nobody@anchorguard.com")).thenReturn(null)

        assertThrows(UnauthorizedException::class.java) {
            service.login(LoginRequest("nobody@anchorguard.com", "Password123!"))
        }
    }

    @Test
    fun `refresh with valid token returns new tokens`() {
        val parents = mock(ParentRepository::class.java)
        val refreshTokens = mock(RefreshTokenRepository::class.java)
        val resetTokens = mock(PasswordResetTokenRepository::class.java)
        val passwordEncoder = mock(PasswordEncoder::class.java)
        val jwtService = JwtService(jwtSecret, 30L, 7L)
        val service = AuthService(parents, refreshTokens, resetTokens, passwordEncoder, jwtService)

        val parentId = UUID.randomUUID()
        val refreshToken = jwtService.generateRefreshToken(parentId, "alice@anchorguard.com")
        val hash = TokenUtils.sha256(refreshToken)
        val stored = RefreshToken(
            parentId = parentId,
            tokenHash = hash,
            expiresAt = Instant.now().plusSeconds(86400 * 30)
        )
        `when`(refreshTokens.findByTokenHash(hash)).thenReturn(stored)
        val parent = Parent(id = parentId, email = "alice@anchorguard.com", passwordHash = "hashed")
        `when`(parents.findById(parentId)).thenReturn(java.util.Optional.of(parent))

        val result = service.refresh(RefreshRequest(refreshToken))

        assertNotNull(result.accessToken)
        assertNotNull(result.refreshToken)
        assertTrue(result.accessToken.isNotEmpty())
        assertTrue(result.refreshToken.isNotEmpty())
        verify(refreshTokens, org.mockito.Mockito.atLeastOnce()).save(org.mockito.ArgumentMatchers.any())
    }

    @Test
    fun `refresh with invalid token throws UnauthorizedException`() {
        val parents = mock(ParentRepository::class.java)
        val refreshTokens = mock(RefreshTokenRepository::class.java)
        val resetTokens = mock(PasswordResetTokenRepository::class.java)
        val passwordEncoder = mock(PasswordEncoder::class.java)
        val jwtService = JwtService(jwtSecret, 30L, 7L)
        val service = AuthService(parents, refreshTokens, resetTokens, passwordEncoder, jwtService)

        assertThrows(UnauthorizedException::class.java) {
            service.refresh(RefreshRequest("bad-token"))
        }
    }

    @Test
    fun `logout revokes refresh token`() {
        val parents = mock(ParentRepository::class.java)
        val refreshTokens = mock(RefreshTokenRepository::class.java)
        val resetTokens = mock(PasswordResetTokenRepository::class.java)
        val passwordEncoder = mock(PasswordEncoder::class.java)
        val jwtService = JwtService(jwtSecret, 30L, 7L)
        val service = AuthService(parents, refreshTokens, resetTokens, passwordEncoder, jwtService)

        val refreshToken = jwtService.generateRefreshToken(UUID.randomUUID(), "alice@anchorguard.com")
        val hash = TokenUtils.sha256(refreshToken)
        val stored = RefreshToken(
            parentId = UUID.randomUUID(),
            tokenHash = hash,
            expiresAt = Instant.now().plusSeconds(86400 * 30)
        )
        `when`(refreshTokens.findByTokenHash(hash)).thenReturn(stored)

        service.logout(LogoutRequest(refreshToken))

        verify(refreshTokens).save(org.mockito.ArgumentMatchers.any())
        assertNotNull(stored.revokedAt)
        assertTrue(stored.revokedAt!!.isAfter(Instant.now().minusSeconds(60)))
    }

    @Test
    fun `requestPasswordReset for existing email creates reset token`() {
        val parents = mock(ParentRepository::class.java)
        val refreshTokens = mock(RefreshTokenRepository::class.java)
        val resetTokens = mock(PasswordResetTokenRepository::class.java)
        val passwordEncoder = mock(PasswordEncoder::class.java)
        val jwtService = JwtService(jwtSecret, 30L, 7L)
        val service = AuthService(parents, refreshTokens, resetTokens, passwordEncoder, jwtService)

        val parent = Parent(id = UUID.randomUUID(), email = "alice@anchorguard.com", passwordHash = "hashed")
        `when`(parents.findByEmail("alice@anchorguard.com")).thenReturn(parent)

        val result = service.requestPasswordReset(PasswordResetRequest("alice@anchorguard.com"))

        assertEquals("If the account exists, a reset token has been issued.", result.message)
        verify(resetTokens).save(org.mockito.ArgumentMatchers.any())
    }

    @Test
    fun `requestPasswordReset for unknown email does not throw`() {
        val parents = mock(ParentRepository::class.java)
        val refreshTokens = mock(RefreshTokenRepository::class.java)
        val resetTokens = mock(PasswordResetTokenRepository::class.java)
        val passwordEncoder = mock(PasswordEncoder::class.java)
        val jwtService = JwtService(jwtSecret, 30L, 7L)
        val service = AuthService(parents, refreshTokens, resetTokens, passwordEncoder, jwtService)

        `when`(parents.findByEmail("nobody@anchorguard.com")).thenReturn(null)

        val result = service.requestPasswordReset(PasswordResetRequest("nobody@anchorguard.com"))

        assertEquals("If the account exists, a reset token has been issued.", result.message)
    }

    @Test
    fun `confirmPasswordReset with valid token updates password`() {
        val parents = mock(ParentRepository::class.java)
        val refreshTokens = mock(RefreshTokenRepository::class.java)
        val resetTokens = mock(PasswordResetTokenRepository::class.java)
        val passwordEncoder = mock(PasswordEncoder::class.java)
        val jwtService = JwtService(jwtSecret, 30L, 7L)
        val service = AuthService(parents, refreshTokens, resetTokens, passwordEncoder, jwtService)

        val parentId = UUID.randomUUID()
        val resetToken = "reset-token"
        val hash = TokenUtils.sha256(resetToken)
        val resetTokenEntity = PasswordResetToken(
            parentId = parentId,
            tokenHash = hash,
            expiresAt = Instant.now().plusSeconds(3600)
        )
        `when`(resetTokens.findByTokenHash(hash)).thenReturn(resetTokenEntity)
        val parent = Parent(id = parentId, email = "alice@anchorguard.com", passwordHash = "old-hash")
        `when`(parents.findById(parentId)).thenReturn(java.util.Optional.of(parent))
        `when`(passwordEncoder.encode("NewPassword123!")).thenReturn("new-hashed")

        val result = service.confirmPasswordReset(
            PasswordResetConfirmRequest(token = resetToken, newPassword = "NewPassword123!")
        )

        assertEquals("Password updated", result.message)
        verify(parents).save(org.mockito.ArgumentMatchers.any())
        verify(refreshTokens).deleteByParentId(parentId)
    }

    @Test
    fun `confirmPasswordReset with invalid token throws BadRequestException`() {
        val parents = mock(ParentRepository::class.java)
        val refreshTokens = mock(RefreshTokenRepository::class.java)
        val resetTokens = mock(PasswordResetTokenRepository::class.java)
        val passwordEncoder = mock(PasswordEncoder::class.java)
        val jwtService = JwtService(jwtSecret, 30L, 7L)
        val service = AuthService(parents, refreshTokens, resetTokens, passwordEncoder, jwtService)

        `when`(resetTokens.findByTokenHash(anyString())).thenReturn(null)

        assertThrows(BadRequestException::class.java) {
            service.confirmPasswordReset(
                PasswordResetConfirmRequest(token = "reset-token", newPassword = "NewPassword123!")
            )
        }
    }

    @Test
    fun `changePassword with wrong current password throws BadRequestException`() {
        val parents = mock(ParentRepository::class.java)
        val refreshTokens = mock(RefreshTokenRepository::class.java)
        val resetTokens = mock(PasswordResetTokenRepository::class.java)
        val passwordEncoder = mock(PasswordEncoder::class.java)
        val jwtService = JwtService(jwtSecret, 30L, 7L)
        val service = AuthService(parents, refreshTokens, resetTokens, passwordEncoder, jwtService)

        val principal = com.anchorguard.api.security.AuthPrincipal(UUID.randomUUID(), "test@anchorguard.com")
        val parent = Parent(id = principal.parentId, email = "alice@anchorguard.com", passwordHash = "hashed")
        `when`(parents.findById(principal.parentId)).thenReturn(java.util.Optional.of(parent))
        `when`(passwordEncoder.matches("wrong", "hashed")).thenReturn(false)

        assertThrows(BadRequestException::class.java) {
            service.changePassword(principal, "wrong", "NewPassword123!")
        }
    }

    @Test
    fun `deleteAccount removes parent`() {
        val parents = mock(ParentRepository::class.java)
        val refreshTokens = mock(RefreshTokenRepository::class.java)
        val resetTokens = mock(PasswordResetTokenRepository::class.java)
        val passwordEncoder = mock(PasswordEncoder::class.java)
        val jwtService = JwtService(jwtSecret, 30L, 7L)
        val service = AuthService(parents, refreshTokens, resetTokens, passwordEncoder, jwtService)

        val principal = com.anchorguard.api.security.AuthPrincipal(UUID.randomUUID(), "test@anchorguard.com")
        val parent = Parent(id = principal.parentId, email = "alice@anchorguard.com", passwordHash = "hashed")
        `when`(parents.findById(principal.parentId)).thenReturn(java.util.Optional.of(parent))

        val result = service.deleteAccount(principal)

        assertEquals("Account deleted", result.message)
        verify(parents).delete(parent)
    }
}
