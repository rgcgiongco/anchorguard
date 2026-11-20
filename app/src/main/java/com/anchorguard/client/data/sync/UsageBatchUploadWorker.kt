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
import com.anchorguard.client.network.ConnectivityState
import com.anchorguard.client.network.NetworkModule
import com.anchorguard.client.network.NetworkResult
import com.anchorguard.client.network.safeApiCall
import java.util.concurrent.TimeUnit

class UsageBatchUploadWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val prefs = SyncPreferences(applicationContext)
        val deviceId = prefs.deviceId() ?: return Result.success()
        if (!ConnectivityState.isOnline(applicationContext)) return Result.retry()

        val repo = UsageSyncRepository(applicationContext)
        val batch = repo.getUnsyncedBatch(BATCH_LIMIT)
        if (batch.isEmpty()) {
            repo.updatePendingCount()
            return Result.success()
        }

        val payload = UsageBatchRequest(
            deviceId = deviceId,
            events = batch.map {
                UsageBatchEvent(
                    id = it.id,
                    deviceUserId = it.deviceUserId,
                    packageName = it.packageName,
                    eventType = it.eventType,
                    startedAt = it.startedAt,
                    endedAt = it.endedAt,
                    durationMs = it.durationMs
                )
            }
        )

        val api = NetworkModule.syncApi(applicationContext)
        return try {
            when (val call = safeApiCall(applicationContext) { api.uploadUsageBatch(payload) }) {
                is NetworkResult.Success -> {
                    val response = call.data
                    val accepted = response.acceptedIds.toSet()
                    val rejected = response.rejectedIds.toSet()
                    if (accepted.isNotEmpty()) {
                        repo.markAccepted(accepted.toList())
                    }
                    // Treat events not explicitly accepted as needing retry/quarantine unless server rejected them
                    val unresolved = batch.map { it.id }.filter { it !in accepted && it !in rejected }.toSet()
                    val toQuarantine = (rejected + unresolved)
                    if (toQuarantine.isNotEmpty()) {
                        repo.quarantineRejected(batch, toQuarantine, UsageSyncRepository.MAX_UPLOAD_RETRIES)
                    } else {
                        repo.updatePendingCount()
                    }
                    Result.success()
                }
                is NetworkResult.Failure -> {
                    val err = call.error
                    when (err) {
                        is com.anchorguard.client.network.NetworkError.Auth -> {
                            Log.w(TAG, "Usage upload auth failure; clearing session handled by authenticator if applicable")
                            Result.failure()
                        }
                        is com.anchorguard.client.network.NetworkError.TierRequired -> {
                            Log.w(TAG, "Usage upload tier required")
                            Result.failure()
                        }
                        is com.anchorguard.client.network.NetworkError.Offline -> Result.retry()
                        is com.anchorguard.client.network.NetworkError.Timeout -> Result.retry()
                        is com.anchorguard.client.network.NetworkError.Server -> Result.retry()
                        is com.anchorguard.client.network.NetworkError.Unknown -> Result.retry()
                    }
                }
            }
        } catch (t: Throwable) {
            Log.w(TAG, "Usage batch upload error", t)
            if (!ConnectivityState.isOnline(applicationContext)) Result.retry() else Result.retry()
        }
    }

    companion object {
        private const val TAG = "UsageBatchUpload"
        private const val BATCH_LIMIT = 100
        const val UNIQUE_WORK_NAME = "anchor_usage_batch_sync"

        fun enqueue(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            val req = OneTimeWorkRequestBuilder<UsageBatchUploadWorker>()
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
