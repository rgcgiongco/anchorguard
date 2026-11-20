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
import com.anchorguard.client.data.local.AnchorGuardDatabase
import com.anchorguard.client.data.local.entity.AppPolicy
import com.anchorguard.client.data.local.entity.DeviceSetting
import com.anchorguard.client.data.local.entity.DeviceUser
import com.anchorguard.client.data.local.entity.SyncStatus
import com.anchorguard.client.enforcement.PolicyEngine
import com.anchorguard.client.network.ConnectivityState
import com.anchorguard.client.network.NetworkModule
import com.anchorguard.client.network.NetworkResult
import com.anchorguard.client.network.safeApiCall
import java.util.UUID
import java.util.concurrent.TimeUnit

class PolicyDownloadWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val prefs = SyncPreferences(applicationContext)
        val deviceId = prefs.deviceId() ?: return Result.success()
        if (!ConnectivityState.isOnline(applicationContext)) return Result.retry()

        val db = AnchorGuardDatabase.getInstance(applicationContext)
        val since = prefs.lastPolicySyncAt(SyncPreferences.TABLE_APP_POLICIES).takeIf { it > 0 }
        val api = NetworkModule.syncApi(applicationContext)

        return when (val call = safeApiCall(applicationContext) { api.getPolicies(deviceId = deviceId, since = since) }) {
            is NetworkResult.Success -> {
                try {
                    applySnapshot(db, prefs, deviceId, call.data)
                    Result.success()
                } catch (t: Throwable) {
                    Log.w(TAG, "Policy apply error", t)
                    Result.retry()
                }
            }
            is NetworkResult.Failure -> when (call.error) {
                is com.anchorguard.client.network.NetworkError.Auth -> Result.failure()
                is com.anchorguard.client.network.NetworkError.TierRequired -> Result.failure()
                is com.anchorguard.client.network.NetworkError.Offline,
                is com.anchorguard.client.network.NetworkError.Timeout,
                is com.anchorguard.client.network.NetworkError.Server,
                is com.anchorguard.client.network.NetworkError.Unknown -> Result.retry()
            }
        }
    }

    private suspend fun applySnapshot(
        db: AnchorGuardDatabase,
        prefs: SyncPreferences,
        deviceId: String,
        body: PoliciesResponse
    ) {
        val now = body.serverTime ?: System.currentTimeMillis()

        // Cloud wins merge: upsert downloaded rows and remove stale ones for this device/scope where applicable.
        if (body.deviceUsers.isNotEmpty()) {
            val remoteUsers = body.deviceUsers.map { it.toEntity(deviceId) }
            val remoteIds = remoteUsers.map { it.id }.toSet()
            val existing = db.deviceUserDao().getByDeviceIdOnce(deviceId)
            existing.filter { it.id !in remoteIds }.forEach { db.deviceUserDao().deleteById(it.id) }
            db.deviceUserDao().insertAll(remoteUsers)
            db.syncStatusDao().upsert(
                SyncStatus(SyncPreferences.TABLE_DEVICE_USERS, SyncPreferences.TABLE_DEVICE_USERS, now, 0)
            )
        }

        if (body.appPolicies.isNotEmpty()) {
            val remotePolicies = body.appPolicies.map { it.toEntity() }
            val remoteIds = remotePolicies.map { it.id }.toSet()
            val existing = db.appPolicyDao().getAllOnce()
            existing.filter { it.id !in remoteIds }.forEach { db.appPolicyDao().deleteById(it.id) }
            db.appPolicyDao().insertAll(remotePolicies)
            db.syncStatusDao().upsert(
                SyncStatus(SyncPreferences.TABLE_APP_POLICIES, SyncPreferences.TABLE_APP_POLICIES, now, 0)
            )
            prefs.setLastPolicySyncAt(SyncPreferences.TABLE_APP_POLICIES, now)

            val blocked = remotePolicies.filter { it.blocked }.map { it.packageName }.toSet()
            PolicyEngine(applicationContext).setBlockedPackages(blocked)
        } else if (body.serverTime != null) {
            // Snapshot indicates success but no policies: clear local cache to reflect cloud state
            db.appPolicyDao().clear()
            PolicyEngine(applicationContext).setBlockedPackages(emptySet())
            prefs.setLastPolicySyncAt(SyncPreferences.TABLE_APP_POLICIES, now)
            db.syncStatusDao().upsert(
                SyncStatus(SyncPreferences.TABLE_APP_POLICIES, SyncPreferences.TABLE_APP_POLICIES, now, 0)
            )
        }

        if (body.deviceSettings.isNotEmpty()) {
            val settings = body.deviceSettings.map { it.toEntity(deviceId) }
            val remoteIds = settings.map { it.id }.toSet()
            val existing = db.deviceSettingDao().getAllOnce(deviceId)
            existing.filter { it.id !in remoteIds }.forEach { db.deviceSettingDao().delete(it.deviceId, it.key) }
            db.deviceSettingDao().setAll(settings)
            db.syncStatusDao().upsert(
                SyncStatus(SyncPreferences.TABLE_DEVICE_SETTINGS, SyncPreferences.TABLE_DEVICE_SETTINGS, now, 0)
            )
            val engine = PolicyEngine(applicationContext)
            settings.firstOrNull { it.key == "block_interval_ms" }?.value?.toLongOrNull()?.let {
                engine.setBlockIntervalMs(it)
            }
        }
    }

    private fun RemoteDeviceUser.toEntity(fallbackDeviceId: String): DeviceUser = DeviceUser(
        id = id.ifBlank { UUID.randomUUID().toString() },
        deviceId = deviceId.ifBlank { fallbackDeviceId },
        childName = childName,
        ageBirthday = ageBirthday,
        avatarColor = avatarColor,
        isPrimary = isPrimary
    )

    private fun RemoteAppPolicy.toEntity(): AppPolicy = AppPolicy(
        id = id.ifBlank { UUID.randomUUID().toString() },
        deviceUserId = deviceUserId?.takeIf { it.isNotBlank() } ?: "unknown-device-user",
        packageName = packageName,
        appName = appName,
        category = category,
        blocked = blocked,
        dailyLimitMinutes = dailyLimitMinutes,
        scheduleStart = scheduleStart,
        scheduleEnd = scheduleEnd,
        weekdaysMask = weekdaysMask,
        blockedMessage = blockedMessage
    )

    private fun RemoteDeviceSetting.toEntity(fallbackDeviceId: String): DeviceSetting = DeviceSetting(
        id = id?.takeIf { it.isNotBlank() } ?: "${fallbackDeviceId}:${key}",
        deviceId = deviceId.ifBlank { fallbackDeviceId },
        key = key,
        value = value,
        updatedAt = updatedAt ?: System.currentTimeMillis()
    )

    companion object {
        private const val TAG = "PolicyDownload"
        const val UNIQUE_WORK_NAME = "anchor_policy_download_sync"

        fun enqueue(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            val req = OneTimeWorkRequestBuilder<PolicyDownloadWorker>()
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
