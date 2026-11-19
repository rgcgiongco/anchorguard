package com.anchorguard.api.admin

import com.anchorguard.api.security.AuthPrincipal
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.time.Instant

@RestController
@RequestMapping("/v1/admin")
class AdminController(
    private val adminUsers: AdminUserRepository,
    private val health: SystemHealthService
) {
    @GetMapping("/dashboard")
    fun dashboard(@AuthenticationPrincipal principal: AuthPrincipal): AdminDashboardResponse {
        val rows = adminUsers.adminUserRows()
        val users = rows.map { r ->
            AdminUserResponse(
                id = r.id,
                email = r.email,
                name = r.name,
                role = if (r.roleId == 2.toShort()) "admin" else "parent",
                emailVerified = r.emailVerified,
                deviceCount = r.deviceCount,
                createdAt = r.createdAt,
                lastSeen = r.lastSeen
            )
        }
        val adminCount = users.count { it.role == "admin" }.toLong()
        val totalDevices = users.sumOf { it.deviceCount }
        return AdminDashboardResponse(
            users = users,
            totalUsers = users.size.toLong(),
            adminCount = adminCount,
            parentCount = users.size - adminCount,
            totalDevices = totalDevices,
            onlineDevices = 0L
        )
    }

    @GetMapping("/health")
    fun systemHealth(@AuthenticationPrincipal principal: AuthPrincipal): SystemHealthResponse = health.snapshot()
}