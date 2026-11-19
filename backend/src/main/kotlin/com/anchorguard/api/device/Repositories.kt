package com.anchorguard.api.device

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface DeviceRepository : JpaRepository<Device, UUID> {
    fun findByHexId(hexId: String): Device?
    fun findByPairingCode(pairingCode: String): Device?
    fun findAllByParentId(parentId: UUID): List<Device>
}

interface DeviceUserRepository : JpaRepository<DeviceUser, UUID> {
    fun findAllByDeviceId(deviceId: UUID): List<DeviceUser>
}

interface SosEventRepository : JpaRepository<SosEvent, UUID> {
    fun findAllByDeviceIdOrderByOccurredAtDesc(deviceId: UUID): List<SosEvent>
}
