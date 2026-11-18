package com.anchorguard.api.device

import jakarta.persistence.*
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

@Entity
@Table(name = "devices")
class Device(
    @Id val id: UUID = UUID.randomUUID(),
    @Column(name = "hex_id", nullable = false, unique = true) var hexId: String,
    @Column(name = "parent_id") var parentId: UUID? = null,
    var name: String? = null,
    var model: String? = null,
    var manufacturer: String? = null,
    @Column(name = "android_id") var androidId: String? = null,
    @Column(name = "app_version") var appVersion: String? = null,
    @Column(name = "api_key_hash") var apiKeyHash: String? = null,
    @Column(name = "last_seen") var lastSeen: Instant? = null,
    @Column(name = "is_online") var isOnline: Boolean = false,
    @Column(name = "pairing_code") var pairingCode: String? = null,
    @Column(name = "pairing_expires_at") var pairingExpiresAt: Instant? = null,
    @Column(name = "created_at") var createdAt: Instant = Instant.now()
)

@Entity
@Table(name = "device_users")
class DeviceUser(
    @Id val id: UUID = UUID.randomUUID(),
    @Column(name = "device_id", nullable = false) var deviceId: UUID,

    // Legacy V1 column kept for backward compatibility
    @Column(nullable = false) var name: String,

    // Spec-aligned columns (V2+)
    @Column(name = "child_name") var childName: String? = null,
    @Column(name = "age_birthday") var ageBirthday: LocalDate? = null,
    @Column(name = "avatar_color") var avatarColor: String? = null,
    @Column(name = "is_primary") var isPrimary: Boolean = false,

    @Column(name = "pin_hash") var pinHash: String? = null,
    @Column(name = "created_at") var createdAt: Instant = Instant.now()
)
