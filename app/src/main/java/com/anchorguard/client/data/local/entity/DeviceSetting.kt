package com.anchorguard.client.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "device_settings",
    indices = [
        Index(value = ["deviceId"]),
        Index(value = ["deviceId", "key"], unique = true)
    ]
)
data class DeviceSetting(
    @PrimaryKey val id: String,
    val deviceId: String,
    val key: String,
    val value: String,
    val updatedAt: Long
)
