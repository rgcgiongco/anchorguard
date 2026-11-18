package com.anchorguard.client.data.sync

import android.content.Context
import com.anchorguard.client.data.local.AnchorGuardDatabase
import com.anchorguard.client.data.local.entity.SyncStatus
import com.anchorguard.client.data.local.entity.UsageEvent
import java.util.UUID

class UsageSyncRepository(context: Context) {
    private val appContext = context.applicationContext
    private val db = AnchorGuardDatabase.getInstance(appContext)
    private val usageDao = db.usageEventDao()
    private val syncStatusDao = db.syncStatusDao()

    suspend fun recordEvent(
        deviceUserId: String,
        packageName: String,
        eventType: String,
        startedAt: Long = System.currentTimeMillis(),
        endedAt: Long? = null,
        durationMs: Long? = null
    ): UsageEvent {
        val event = UsageEvent(
            id = UUID.randomUUID().toString(),
            deviceUserId = deviceUserId,
            packageName = packageName,
            eventType = eventType,
            startedAt = startedAt,
            endedAt = endedAt,
            durationMs = durationMs,
            synced = false,
            retryCount = 0,
            deadLetter = false
        )
        usageDao.insert(event)
        updatePendingCount()
        return event
    }

    suspend fun getUnsyncedBatch(limit: Int = 100): List<UsageEvent> = usageDao.getUnsynced(limit)

    suspend fun markAccepted(ids: List<String>, syncedAt: Long = System.currentTimeMillis()) {
        if (ids.isEmpty()) {
            updatePendingCount(syncedAt)
            return
        }
        usageDao.markSynced(ids)
        updatePendingCount(syncedAt)
    }

    suspend fun quarantineRejected(events: List<UsageEvent>, rejectedIds: Set<String>, maxRetries: Int = MAX_UPLOAD_RETRIES) {
        if (rejectedIds.isEmpty()) {
            updatePendingCount()
            return
        }
        val rejected = events.filter { it.id in rejectedIds }
        if (rejected.isEmpty()) {
            updatePendingCount()
            return
        }
        val toDeadLetter = rejected.filter { it.retryCount + 1 >= maxRetries }.map { it.id }
        val toRetry = rejected.filter { it.retryCount + 1 < maxRetries }.map { it.id }
        if (toRetry.isNotEmpty()) usageDao.incrementRetry(toRetry)
        if (toDeadLetter.isNotEmpty()) usageDao.markDeadLetter(toDeadLetter)
        updatePendingCount()
    }

    suspend fun updatePendingCount(lastSyncedAt: Long? = null) {
        val pending = usageDao.countPending()
        val existing = syncStatusDao.get(SyncPreferences.TABLE_USAGE_EVENTS)
        syncStatusDao.upsert(
            SyncStatus(
                id = SyncPreferences.TABLE_USAGE_EVENTS,
                tableName = SyncPreferences.TABLE_USAGE_EVENTS,
                lastSyncedAt = lastSyncedAt ?: existing?.lastSyncedAt,
                pendingCount = pending
            )
        )
    }

    suspend fun countPending(): Int = usageDao.countPending()
    suspend fun countDeadLetter(): Int = usageDao.countDeadLetter()

    companion object {
        const val MAX_UPLOAD_RETRIES = 5
    }
}
