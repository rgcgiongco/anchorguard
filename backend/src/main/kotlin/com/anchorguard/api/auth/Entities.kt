package com.anchorguard.api.auth

import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "parents")
class Parent(
    @Id val id: UUID = UUID.randomUUID(),
    @Column(nullable = false, unique = true) var email: String,
    @Column(name = "password_hash", nullable = false) var passwordHash: String,
    var name: String? = null,
    @Column(name = "email_verified") var emailVerified: Boolean = false,
    @Column(name = "role_id", nullable = false) var roleId: Short = 1,
    @Column(name = "created_at") var createdAt: Instant = Instant.now(),
    @Column(name = "updated_at") var updatedAt: Instant = Instant.now(),
    @Column(name = "parent_pin_hash") var parentPinHash: String? = null,
    @Column(name = "tier", nullable = false) var tier: String = "FREE",
    @Column(name = "device_limit", nullable = false) var deviceLimit: Int = 1,
    @Column(name = "allow_schedules", nullable = false) var allowSchedules: Boolean = false,
    @Column(name = "email_support", nullable = false) var emailSupport: Boolean = false
) {
    val isAdmin: Boolean get() = roleId == 2.toShort()
    val roleName: String get() = if (isAdmin) "admin" else "parent"
    val isFreeTier: Boolean get() = tier == "FREE"
}

@Entity
@Table(name = "roles")
class Role(
    @Id @Column(name = "role_id") val roleId: Short,
    @Column(nullable = false, unique = true) val name: String,
    val description: String? = null,
    @Column(name = "is_system") val isSystem: Boolean = false
)

@Entity
@Table(name = "refresh_tokens")
class RefreshToken(
    @Id val id: UUID = UUID.randomUUID(),
    @Column(name = "parent_id", nullable = false) val parentId: UUID,
    @Column(name = "token_hash", nullable = false, unique = true) val tokenHash: String,
    @Column(name = "expires_at", nullable = false) val expiresAt: Instant,
    @Column(name = "revoked_at") var revokedAt: Instant? = null,
    @Column(name = "created_at") val createdAt: Instant = Instant.now()
)

@Entity
@Table(name = "password_reset_tokens")
class PasswordResetToken(
    @Id val id: UUID = UUID.randomUUID(),
    @Column(name = "parent_id", nullable = false) val parentId: UUID,
    @Column(name = "token_hash", nullable = false, unique = true) val tokenHash: String,
    @Column(name = "expires_at", nullable = false) val expiresAt: Instant,
    @Column(name = "used_at") var usedAt: Instant? = null,
    @Column(name = "created_at") val createdAt: Instant = Instant.now()
)