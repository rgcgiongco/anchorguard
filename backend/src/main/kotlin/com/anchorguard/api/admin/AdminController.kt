package com.anchorguard.api.admin

import com.anchorguard.api.device.DeviceRepository
import com.anchorguard.api.device.DeviceRole
import com.anchorguard.api.security.AuthPrincipal
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.time.Instant

@RestController
@RequestMapping("/v1/admin")
class AdminController(
    private val adminUsers: AdminUserRepository,
    private val health: SystemHealthService,
    private val devices: DeviceRepository
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

    @GetMapping("/devices/shared-mode")
    fun sharedModeDevices(@AuthenticationPrincipal principal: AuthPrincipal): List<SharedModeDeviceDto> =
        devices.findByRole(DeviceRole.BOTH).map { d ->
            SharedModeDeviceDto(
                id = d.id,
                hexId = d.hexId,
                name = d.name,
                activeMode = d.activeMode,
                lastModeSwitchAt = d.lastModeSwitchAt
            )
        }

    @GetMapping("/health")
    fun systemHealth(@AuthenticationPrincipal principal: AuthPrincipal): SystemHealthResponse = health.snapshot()
}

data class SharedModeDeviceDto(
    val id: java.util.UUID,
    val hexId: String,
    val name: String?,
    val activeMode: String,
    val lastModeSwitchAt: java.time.LocalDateTime?
)