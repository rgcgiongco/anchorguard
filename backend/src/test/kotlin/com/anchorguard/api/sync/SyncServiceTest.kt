package com.anchorguard.api.sync

import com.anchorguard.api.common.NotFoundException
import com.anchorguard.api.device.DeviceService
import com.anchorguard.api.device.DeviceUserRepository
import com.anchorguard.api.policy.AppPolicyRepository
import com.anchorguard.api.policy.DeviceSettingsRepository
import com.anchorguard.api.report.UsageEvent
import com.anchorguard.api.report.UsageEventRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import org.mockito.Mockito.mock
import org.mockito.junit.jupiter.MockitoExtension
import java.time.Instant
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class SyncServiceTest {

    private val deviceService = mock(DeviceService::class.java)
    private val usageEvents = mock(UsageEventRepository::class.java)
    private val deviceUsers = mock(DeviceUserRepository::class.java)
    private val appPolicies = mock(AppPolicyRepository::class.java)
    private val settingsRepo = mock(DeviceSettingsRepository::class.java)

    private val service = SyncService(deviceService, usageEvents, deviceUsers, appPolicies, settingsRepo)

    @Test
    fun `ingestUsageBatch accepts valid events and rejects blank ones`() {
        val deviceId = UUID.randomUUID()
        val apiKey = "valid-key"
        val device = com.anchorguard.api.device.Device(
            id = deviceId,
            hexId = "abc123",
            apiKeyHash = "hash-of-" + apiKey
        )
        `when`(deviceService.deviceByIdWithKey(deviceId, apiKey)).thenReturn(device)
        `when`(usageEvents.saveAll(any<List<UsageEvent>>())).thenAnswer { it.getArgument<List<UsageEvent>>(0) }

        val req = com.anchorguard.api.sync.UsageBatchRequest(
            deviceId = deviceId,
            events = listOf(
                com.anchorguard.api.sync.UsageBatchEventDto(
                    id = "evt-1",
                    packageName = "com.android.chrome",
                    eventType = "FOREGROUND",
                    startedAt = System.currentTimeMillis()
                ),
                com.anchorguard.api.sync.UsageBatchEventDto(
                    id = "evt-2",
                    packageName = "",
                    eventType = "FOREGROUND"
                )
            )
        )

        val result = service.ingestUsageBatch(deviceId, apiKey, req)

        assertEquals(1, result.acceptedIds.size)
        assertEquals("evt-1", result.acceptedIds[0])
        assertEquals(1, result.rejectedIds.size)
        assertEquals("evt-2", result.rejectedIds[0])
        verify(usageEvents).saveAll(any<List<UsageEvent>>())
    }

    @Test
    fun `ingestUsageBatch with non-existent device throws NotFoundException`() {
        val deviceId = UUID.randomUUID()
        val apiKey = "valid-key"
        `when`(deviceService.deviceByIdWithKey(deviceId, apiKey))
            .thenThrow(com.anchorguard.api.common.NotFoundException("Device not found"))

        val req = com.anchorguard.api.sync.UsageBatchRequest(
            deviceId = deviceId,
            events = emptyList()
        )

        assertThrows(com.anchorguard.api.common.NotFoundException::class.java) {
            service.ingestUsageBatch(deviceId, apiKey, req)
        }
    }

    @Test
    fun `getPoliciesForSync returns full snapshot for device key auth`() {
        val deviceId = UUID.randomUUID()
        val apiKey = "valid-key"
        val device = com.anchorguard.api.device.Device(
            id = deviceId,
            hexId = "abc123",
            apiKeyHash = "hash-of-" + apiKey
        )
        `when`(deviceService.deviceByIdWithKey(deviceId, apiKey)).thenReturn(device)
        `when`(deviceUsers.findAllByDeviceId(deviceId)).thenReturn(emptyList())
        `when`(appPolicies.findAllByDeviceId(deviceId)).thenReturn(emptyList())
        `when`(settingsRepo.findById(deviceId)).thenReturn(java.util.Optional.empty())

        val result = service.getPoliciesForSync(null, deviceId, apiKey, null)

        assertEquals(deviceId, result.deviceId)
        assertTrue(result.deviceUsers.isEmpty())
        assertTrue(result.appPolicies.isEmpty())
        assertTrue(result.deviceSettings.isEmpty())
        assertNotNull(result.serverTime)
    }
}
