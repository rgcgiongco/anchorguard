package com.anchorguard.client.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.anchorguard.client.data.local.entity.SyncStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface SyncStatusDao {
    @Query("SELECT * FROM sync_status WHERE tableName = :tableName LIMIT 1")
    suspend fun get(tableName: String): SyncStatus?

    @Query("SELECT * FROM sync_status ORDER BY tableName ASC")
    fun getAll(): Flow<List<SyncStatus>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(status: SyncStatus)

    @Update
    suspend fun update(status: SyncStatus)

    @Query("UPDATE sync_status SET lastSyncedAt = :lastSyncedAt, pendingCount = :pendingCount WHERE tableName = :tableName")
    suspend fun update(tableName: String, lastSyncedAt: Long?, pendingCount: Int)

    @Query("DELETE FROM sync_status")
    suspend fun clear()
}
