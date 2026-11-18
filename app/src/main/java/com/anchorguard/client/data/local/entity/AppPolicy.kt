package com.anchorguard.client.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "app_policies",
    indices = [
        Index(value = ["deviceUserId"]),
        Index(value = ["packageName"]),
        Index(value = ["deviceUserId", "packageName"], unique = true)
    ]
)
data class AppPolicy(
    @PrimaryKey val id: String,
    val deviceUserId: String,
    val packageName: String,
    val appName: String? = null,
    val category: String? = null,
    val blocked: Boolean = false,
    val dailyLimitMinutes: Int? = null,
    val scheduleStart: String? = null,
    val scheduleEnd: String? = null,
    val weekdaysMask: Int? = null,
    val blockedMessage: String? = null
)
