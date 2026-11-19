package com.anchorguard.api.admin

import com.anchorguard.api.auth.Parent
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.Instant
import java.util.UUID

interface AdminUserRepository : JpaRepository<Parent, UUID> {
    @Query(
        value = """
            SELECT p.id AS id,
                   p.email AS email,
                   p.name AS name,
                   p.role_id AS roleId,
                   p.email_verified AS emailVerified,
                   p.created_at AS createdAt,
                   (SELECT COUNT(d.id) FROM devices d WHERE d.parent_id = p.id) AS deviceCount,
                   (SELECT MAX(d.last_seen) FROM devices d WHERE d.parent_id = p.id) AS lastSeen
            FROM parents p
            ORDER BY p.created_at DESC
        """,
        nativeQuery = true
    )
    fun adminUserRows(): List<AdminUserRow>
}

interface AdminUserRow {
    val id: UUID
    val email: String
    val name: String?
    val roleId: Short
    val emailVerified: Boolean
    val createdAt: Instant
    val deviceCount: Long
    val lastSeen: Instant?
}