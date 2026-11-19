package com.anchorguard.client.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "geofences",
    indices = [
        Index(value = ["device_id"]),
        Index(value = ["synced"])
    ]
)
data class Geofence(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "device_id") val deviceId: String,
    @ColumnInfo(name = "zone_id") val zoneId: String,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "latitude") val latitude: Double,
    @ColumnInfo(name = "longitude") val longitude: Double,
    @ColumnInfo(name = "radius_meters") val radiusMeters: Int,
    @ColumnInfo(name = "synced") val synced: Boolean = false
)
