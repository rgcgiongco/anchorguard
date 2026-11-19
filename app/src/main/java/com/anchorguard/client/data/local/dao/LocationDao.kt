package com.anchorguard.client.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.anchorguard.client.data.local.entity.Location
import kotlinx.coroutines.flow.Flow

@Dao
interface LocationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(location: Location)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(locations: List<Location>)

    @Query("SELECT * FROM locations WHERE synced = 0 ORDER BY timestamp_millis ASC")
    suspend fun getUnsyncedLocations(): List<Location>

    @Query("UPDATE locations SET synced = 1 WHERE id IN (:ids)")
    suspend fun markAsSynced(ids: List<String>)

    @Query("SELECT * FROM locations WHERE device_id = :deviceId ORDER BY timestamp_millis DESC LIMIT 1")
    suspend fun getLatestLocation(deviceId: String): Location?

    @Query("SELECT * FROM locations WHERE device_id = :deviceId AND timestamp_millis >= :since ORDER BY timestamp_millis DESC")
    suspend fun getLocationsSince(deviceId: String, since: Long): List<Location>

    @Query("SELECT * FROM locations WHERE device_id = :deviceId ORDER BY timestamp_millis DESC LIMIT :limit")
    suspend fun getLocationHistory(deviceId: String, limit: Int): List<Location>

    @Query("SELECT * FROM locations WHERE device_id = :deviceId ORDER BY timestamp_millis DESC")
    fun getRecent(deviceId: String): Flow<List<Location>>

    @Query("DELETE FROM locations WHERE device_id = :deviceId")
    suspend fun clearForDevice(deviceId: String)
}
