package com.anchorguard.api.security

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.nio.charset.StandardCharsets
import java.time.Instant
import java.util.Date
import java.util.UUID
import javax.crypto.SecretKey

@Service
class JwtService(
    @Value("\${anchorguard.security.jwt-secret}") secret: String,
    @Value("\${anchorguard.security.access-token-minutes}") private val accessMinutes: Long,
    @Value("\${anchorguard.security.refresh-token-days}") private val refreshDays: Long
) {
    private val key: SecretKey = Keys.hmacShaKeyFor(secret.toByteArray(StandardCharsets.UTF_8).copyOf(64))

    fun generateAccessToken(parentId: UUID, email: String, role: String = "parent", deviceRole: String? = null, activeMode: String? = null): String =
        buildToken(parentId, email, role, "access", Instant.now().plusSeconds(accessMinutes * 60), deviceRole, activeMode)

    fun generateRefreshToken(parentId: UUID, email: String, role: String = "parent"): String =
        buildToken(parentId, email, role, "refresh", Instant.now().plusSeconds(refreshDays * 86400))

    fun refreshExpiry(): Instant = Instant.now().plusSeconds(refreshDays * 86400)

    private fun buildToken(parentId: UUID, email: String, role: String, type: String, expiresAt: Instant, deviceRole: String? = null, activeMode: String? = null): String =
        Jwts.builder()
            .subject(parentId.toString())
            .claim("email", email)
            .claim("role", role)
            .claim("type", type)
            .also { if (deviceRole != null) it.claim("deviceRole", deviceRole) }
            .also { if (activeMode != null) it.claim("activeMode", activeMode) }
            .issuedAt(Date.from(Instant.now()))
            .expiration(Date.from(expiresAt))
            .signWith(key)
            .compact()

    fun parse(token: String): Claims? = try {
        Jwts.parser().verifyWith(key).build().parseSignedClaims(token).payload
    } catch (_: Exception) {
        null
    }

    fun isAccessToken(claims: Claims): Boolean = claims["type"] == "access"
    fun isRefreshToken(claims: Claims): Boolean = claims["type"] == "refresh"
}
