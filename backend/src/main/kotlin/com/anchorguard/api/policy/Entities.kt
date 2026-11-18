package com.anchorguard.api.policy

import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.Instant
import java.util.UUID

@Entity
@Table(
    name = "app_policies",
    uniqueConstraints = [UniqueConstraint(columnNames = ["device_id", "package_name"])]
)
class AppPolicy(
    @Id val id: UUID = UUID.randomUUID(),

    // Legacy device-scoped linkage (V1/MVP)
    @Column(name = "device_id", nullable = false) var deviceId: UUID,
    @Column(name = "package_name", nullable = false) var packageName: String,
    @Column(name = "app_name") var appName: String? = null,
    @Column(name = "is_allowed") var isAllowed: Boolean = true,
    @Column(name = "daily_limit_minutes") var dailyLimitMinutes: Int? = null,
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "block_schedule", columnDefinition = "jsonb") var blockSchedule: String? = null,
    @Column(name = "internet_access") var internetAccess: String = "on",
    @Column(name = "created_at") var createdAt: Instant = Instant.now(),
    @Column(name = "updated_at") var updatedAt: Instant = Instant.now(),

    // Spec-aligned per-user/scheduling fields (V2+, additive)
    @Column(name = "device_user_id") var deviceUserId: UUID? = null,
    @Column(name = "category") var category: String? = null,
    @Column(name = "blocked") var blocked: Boolean? = null,
    @Column(name = "schedule_start") var scheduleStart: String? = null,
    @Column(name = "schedule_end") var scheduleEnd: String? = null,
    @Column(name = "weekdays_mask") var weekdaysMask: Int? = null,
    @Column(name = "blocked_message") var blockedMessage: String? = null
)

@Entity
@Table(name = "device_settings")
class DeviceSettings(
    @Id @Column(name = "device_id") var deviceId: UUID,
    @Column(name = "advanced_timer") var advancedTimer: Boolean = false,
    @Column(name = "background_mode") var backgroundMode: Boolean = false,
    @Column(name = "tamper_proof") var tamperProof: Boolean = false,
    @Column(name = "boot_start") var bootStart: Boolean = false,
    @Column(name = "keep_alive") var keepAlive: Boolean = false,
    @Column(name = "brute_force_pin") var bruteForcePin: Boolean = false,
    @Column(name = "block_interval_ms") var blockIntervalMs: Int = 500,
    @Column(name = "app_title") var appTitle: String = "AnchorGuard",
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "weekly_schedule", columnDefinition = "jsonb") var weeklySchedule: String? = null,
    @Column(name = "updated_at") var updatedAt: Instant = Instant.now()
)
