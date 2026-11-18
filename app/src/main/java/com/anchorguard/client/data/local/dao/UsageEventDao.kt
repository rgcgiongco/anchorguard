package com.anchorguard.client.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.anchorguard.client.data.local.entity.UsageEvent
import kotlinx.coroutines.flow.Flow

@Dao
interface UsageEventDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(event: UsageEvent)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(events: List<UsageEvent>)

    @Query("UPDATE usage_events SET synced = 1 WHERE id IN (:ids)")
    suspend fun markSynced(ids: List<String>)

    @Query("UPDATE usage_events SET synced = 1 WHERE id = :id")
    suspend fun markSyncedById(id: String)

    @Query("UPDATE usage_events SET retryCount = retryCount + 1 WHERE id IN (:ids)")
    suspend fun incrementRetry(ids: List<String>)

    @Query("UPDATE usage_events SET deadLetter = 1, synced = 0 WHERE id IN (:ids)")
    suspend fun markDeadLetter(ids: List<String>)

    @Query("UPDATE usage_events SET retryCount = :retryCount WHERE id = :id")
    suspend fun setRetryCount(id: String, retryCount: Int)

    @Query("SELECT * FROM usage_events WHERE synced = 0 AND deadLetter = 0 ORDER BY startedAt ASC")
    suspend fun getUnsynced(): List<UsageEvent>

    @Query("SELECT * FROM usage_events WHERE synced = 0 AND deadLetter = 0 ORDER BY startedAt ASC LIMIT :limit")
    suspend fun getUnsynced(limit: Int): List<UsageEvent>

    @Query("SELECT COUNT(*) FROM usage_events WHERE synced = 0 AND deadLetter = 0")
    suspend fun countPending(): Int

    @Query("SELECT COUNT(*) FROM usage_events WHERE deadLetter = 1")
    suspend fun countDeadLetter(): Int

    @Query("SELECT * FROM usage_events ORDER BY startedAt DESC")
    fun getAll(): Flow<List<UsageEvent>>

    @Query("SELECT * FROM usage_events ORDER BY startedAt DESC LIMIT :limit")
    suspend fun getRecent(limit: Int): List<UsageEvent>

    @Query("DELETE FROM usage_events WHERE synced = 1 AND startedAt < :beforeEpochMs")
    suspend fun deleteSyncedBefore(beforeEpochMs: Long)

    @Query("DELETE FROM usage_events")
    suspend fun clear()
}
