package com.anchorguard.client.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.anchorguard.client.data.local.entity.Geofence

@Dao
interface GeofenceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(geofence: Geofence)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(geofences: List<Geofence>)

    @Query("DELETE FROM geofences WHERE device_id = :deviceId")
    suspend fun deleteForDevice(deviceId: String)

    @Query("SELECT * FROM geofences WHERE device_id = :deviceId")
    suspend fun getAllForDevice(deviceId: String): List<Geofence>
}
