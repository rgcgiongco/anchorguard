package com.anchorguard.api.auth

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface ParentRepository : JpaRepository<Parent, UUID> {
    fun findByEmail(email: String): Parent?
    fun existsByEmail(email: String): Boolean
}

interface RefreshTokenRepository : JpaRepository<RefreshToken, UUID> {
    fun findByTokenHash(tokenHash: String): RefreshToken?
    fun deleteByParentId(parentId: UUID)
}

interface PasswordResetTokenRepository : JpaRepository<PasswordResetToken, UUID> {
    fun findByTokenHash(tokenHash: String): PasswordResetToken?
}
