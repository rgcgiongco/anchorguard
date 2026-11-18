package com.anchorguard.client.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "device_users",
    indices = [Index(value = ["deviceId"])]
)
data class DeviceUser(
    @PrimaryKey val id: String,
    val deviceId: String,
    val childName: String,
    val ageBirthday: String? = null,
    val avatarColor: String? = null,
    val isPrimary: Boolean = false
)
