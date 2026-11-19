package com.anchorguard.api.admin

import java.time.Instant
import java.util.UUID

data class MetricPoint(val t: Long, val v: Double)
data class MetricSeries(val key: String, val label: String, val unit: String, val current: Double, val max: Double, val points: List<MetricPoint>)

data class SystemHealthResponse(
    val capturedAt: Instant,
    val status: String,
    val api: ApiStatus,
    val cpu: MetricSeries,
    val ram: MetricSeries,
    val storage: MetricSeries,
    val uptimeSeconds: Long
)

data class ApiStatus(
    val status: String,
    val db: String,
    val diskTotalBytes: Long,
    val diskFreeBytes: Long,
    val diskUsableBytes: Long
)

data class AdminUserResponse(
    val id: UUID,
    val email: String,
    val name: String?,
    val role: String,
    val emailVerified: Boolean,
    val deviceCount: Long,
    val createdAt: Instant,
    val lastSeen: Instant?
)

data class AdminDashboardResponse(
    val users: List<AdminUserResponse>,
    val totalUsers: Long,
    val adminCount: Long,
    val parentCount: Long,
    val totalDevices: Long,
    val onlineDevices: Long
)