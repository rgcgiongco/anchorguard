package com.anchorguard.client.data.sync

import android.content.Context
import android.util.Log
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.anchorguard.client.data.location.LocationRepository
import com.anchorguard.client.network.ConnectivityState
import com.anchorguard.client.network.NetworkModule
import com.anchorguard.client.network.NetworkResult
import com.anchorguard.client.network.safeApiCall
import java.util.concurrent.TimeUnit

class LocationSyncWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val prefs = SyncPreferences(applicationContext)
        val deviceId = prefs.deviceId() ?: return Result.success()
        if (!ConnectivityState.isOnline(applicationContext)) return Result.retry()

        val repo = LocationRepository.instance()
        val unsynced = repo.getUnsynced()
        if (unsynced.isEmpty()) {
            repo.markSynced(emptyList())
            updateSyncStatus(prefs, 0)
            return Result.success()
        }

        val payload = LocationBatchRequest(
            deviceId = deviceId,
            locations = unsynced.map {
                LocationPoint(
                    latitude = it.latitude,
                    longitude = it.longitude,
                    accuracy = it.accuracyMeters.toFloat(),
                    timestamp = it.timestampMillis
                )
            }
        )

        val ids = unsynced.map { it.id }
        val api = NetworkModule.syncApi(applicationContext)
        return try {
            when (val call = safeApiCall(applicationContext) { api.uploadLocationBatch(payload) }) {
                is NetworkResult.Success -> {
                    repo.markSynced(ids)
                    updateSyncStatus(prefs, 0)
                    Log.i(TAG, "Location batch synced: ${ids.size} uploaded")
                    Result.success()
                }
                is NetworkResult.Failure -> {
                    val err = call.error
                    when (err) {
                        is com.anchorguard.client.network.NetworkError.Auth -> {
                            Log.w(TAG, "Location sync auth failure")
                            Result.failure()
                        }
                        is com.anchorguard.client.network.NetworkError.Offline,
                        is com.anchorguard.client.network.NetworkError.Timeout,
                        is com.anchorguard.client.network.NetworkError.Server,
                        is com.anchorguard.client.network.NetworkError.Unknown -> {
                            Log.w(TAG, "Location sync failure: ${err.userMessage()}")
                            Result.retry()
                        }
                    }
                }
            }
        } catch (t: Throwable) {
            Log.w(TAG, "Location batch upload error", t)
            Result.retry()
        }
    }

    private suspend fun updateSyncStatus(prefs: SyncPreferences, pendingCount: Int) {
        try {
            val now = System.currentTimeMillis()
            val db = com.anchorguard.client.data.local.AnchorGuardDatabase.getInstance(applicationContext)
            db.syncStatusDao().upsert(
                com.anchorguard.client.data.local.entity.SyncStatus(
                    id = SyncPreferences.TABLE_LOCATIONS,
                    tableName = SyncPreferences.TABLE_LOCATIONS,
                    lastSyncedAt = now,
                    pendingCount = pendingCount
                )
            )
        } catch (_: Exception) {}
    }

    companion object {
        private const val TAG = "LocationSync"
        const val UNIQUE_WORK_NAME = "anchor_location_batch_sync"

        fun enqueue(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            val req = OneTimeWorkRequestBuilder<LocationSyncWorker>()
                .setConstraints(constraints)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
                .build()
            WorkManager.getInstance(context).enqueueUniqueWork(
                UNIQUE_WORK_NAME,
                ExistingWorkPolicy.KEEP,
                req
            )
        }
    }
}
