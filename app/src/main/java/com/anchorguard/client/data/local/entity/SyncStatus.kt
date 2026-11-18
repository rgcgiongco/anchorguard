package com.anchorguard.client.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sync_status")
data class SyncStatus(
    @PrimaryKey val id: String,
    val tableName: String,
    val lastSyncedAt: Long? = null,
    val pendingCount: Int = 0
)
