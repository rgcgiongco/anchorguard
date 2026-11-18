package com.anchorguard.client.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "usage_events",
    indices = [
        Index(value = ["synced"]),
        Index(value = ["deadLetter"]),
        Index(value = ["deviceUserId"]),
        Index(value = ["startedAt"])
    ]
)
data class UsageEvent(
    @PrimaryKey val id: String,
    val deviceUserId: String,
    val packageName: String,
    val eventType: String,
    val startedAt: Long,
    val endedAt: Long? = null,
    val durationMs: Long? = null,
    val synced: Boolean = false,
    val retryCount: Int = 0,
    val deadLetter: Boolean = false
)
