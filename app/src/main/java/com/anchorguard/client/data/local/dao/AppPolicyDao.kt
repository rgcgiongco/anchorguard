package com.anchorguard.client.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.anchorguard.client.data.local.entity.AppPolicy
import kotlinx.coroutines.flow.Flow

@Dao
interface AppPolicyDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(policy: AppPolicy)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(policies: List<AppPolicy>)

    @Update
    suspend fun update(policy: AppPolicy)

    @Delete
    suspend fun delete(policy: AppPolicy)

    @Query("DELETE FROM app_policies WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT * FROM app_policies ORDER BY appName ASC, packageName ASC")
    fun getAll(): Flow<List<AppPolicy>>

    @Query("SELECT * FROM app_policies")
    suspend fun getAllOnce(): List<AppPolicy>

    @Query("SELECT * FROM app_policies WHERE deviceUserId = :deviceUserId ORDER BY appName ASC, packageName ASC")
    fun getByDeviceUserId(deviceUserId: String): Flow<List<AppPolicy>>

    @Query("SELECT * FROM app_policies WHERE deviceUserId = :deviceUserId")
    suspend fun getByDeviceUserIdOnce(deviceUserId: String): List<AppPolicy>

    @Query("SELECT * FROM app_policies WHERE packageName = :packageName LIMIT 1")
    suspend fun getByPackageName(packageName: String): AppPolicy?

    @Query("SELECT * FROM app_policies WHERE deviceUserId = :deviceUserId AND packageName = :packageName LIMIT 1")
    suspend fun getByDeviceUserAndPackage(deviceUserId: String, packageName: String): AppPolicy?

    @Query("SELECT * FROM app_policies WHERE blocked = 1")
    fun getBlocked(): Flow<List<AppPolicy>>

    @Query("SELECT * FROM app_policies WHERE blocked = 1")
    suspend fun getBlockedOnce(): List<AppPolicy>

    @Query("DELETE FROM app_policies")
    suspend fun clear()
}
