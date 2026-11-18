package com.anchorguard.client.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.anchorguard.client.data.local.entity.DeviceSetting
import kotlinx.coroutines.flow.Flow

@Dao
interface DeviceSettingDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun set(setting: DeviceSetting)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setAll(settings: List<DeviceSetting>)

    @Query("SELECT * FROM device_settings WHERE deviceId = :deviceId AND `key` = :key LIMIT 1")
    suspend fun get(deviceId: String, key: String): DeviceSetting?

    @Query("SELECT * FROM device_settings WHERE deviceId = :deviceId ORDER BY `key` ASC")
    fun getAll(deviceId: String): Flow<List<DeviceSetting>>

    @Query("SELECT * FROM device_settings WHERE deviceId = :deviceId")
    suspend fun getAllOnce(deviceId: String): List<DeviceSetting>

    @Delete
    suspend fun delete(setting: DeviceSetting)

    @Query("DELETE FROM device_settings WHERE deviceId = :deviceId AND `key` = :key")
    suspend fun delete(deviceId: String, key: String)

    @Query("DELETE FROM device_settings WHERE deviceId = :deviceId")
    suspend fun clearForDevice(deviceId: String)
}
