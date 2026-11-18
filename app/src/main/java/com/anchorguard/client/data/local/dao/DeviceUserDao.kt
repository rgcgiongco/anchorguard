package com.anchorguard.client.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.anchorguard.client.data.local.entity.DeviceUser
import kotlinx.coroutines.flow.Flow

@Dao
interface DeviceUserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: DeviceUser)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(users: List<DeviceUser>)

    @Update
    suspend fun update(user: DeviceUser)

    @Delete
    suspend fun delete(user: DeviceUser)

    @Query("DELETE FROM device_users WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT * FROM device_users ORDER BY isPrimary DESC, childName ASC")
    fun getAll(): Flow<List<DeviceUser>>

    @Query("SELECT * FROM device_users WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): DeviceUser?

    @Query("SELECT * FROM device_users WHERE deviceId = :deviceId ORDER BY isPrimary DESC, childName ASC")
    fun getByDeviceId(deviceId: String): Flow<List<DeviceUser>>

    @Query("SELECT * FROM device_users WHERE deviceId = :deviceId ORDER BY isPrimary DESC, childName ASC")
    suspend fun getByDeviceIdOnce(deviceId: String): List<DeviceUser>

    @Query("DELETE FROM device_users")
    suspend fun clear()
}
