package com.anchorguard.client.data.location

import android.content.Context
import com.anchorguard.client.data.local.AnchorGuardDatabase
import com.anchorguard.client.data.local.entity.Location
import com.anchorguard.client.data.sync.LocationSyncScheduler
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationRepository @Inject constructor(
    private val context: Context,
    private val dao: com.anchorguard.client.data.local.dao.LocationDao
) {

    suspend fun saveLocation(lat: Double, long: Double, accuracy: Float, timestamp: Long) {
        val location = Location(
            id = java.util.UUID.randomUUID().toString(),
            deviceId = context.packageName,
            latitude = lat,
            longitude = long,
            accuracyMeters = accuracy.toInt(),
            timestampMillis = timestamp,
            provider = "fused"
        )
        dao.insert(location)
        // Trigger immediate background sync of unsynced locations
        LocationSyncScheduler.enqueueImmediate(context)
    }

    suspend fun getUnsynced(): List<Location> = dao.getUnsyncedLocations()

    suspend fun markSynced(ids: List<String>) {
        if (ids.isNotEmpty()) dao.markAsSynced(ids)
    }

    suspend fun getLocationHistory(deviceId: String, limit: Int): List<Location> =
        dao.getLocationHistory(deviceId, limit)

    suspend fun getLatestLocation(deviceId: String): Location? =
        dao.getLatestLocation(deviceId)

    companion object {
        @Volatile private var instance: LocationRepository? = null

        fun init(context: Context): LocationRepository {
            return instance ?: synchronized(this) {
                instance ?: LocationRepository(
                    context.applicationContext,
                    AnchorGuardDatabase.getInstance(context.applicationContext).locationDao()
                ).also { instance = it }
            }
        }

        fun instance(): LocationRepository = instance ?: error("LocationRepository not initialized.")
    }
}
