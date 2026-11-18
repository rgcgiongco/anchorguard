package com.anchorguard.api.policy

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface AppPolicyRepository : JpaRepository<AppPolicy, UUID> {
    fun findAllByDeviceId(deviceId: UUID): List<AppPolicy>
    fun findByDeviceIdAndPackageName(deviceId: UUID, packageName: String): AppPolicy?
    fun deleteByDeviceId(deviceId: UUID)
}

interface DeviceSettingsRepository : JpaRepository<DeviceSettings, UUID>
