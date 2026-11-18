package com.anchorguard.client.data.sync

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.anchorguard.client.network.ConnectivityState
import java.util.concurrent.TimeUnit

object SyncScheduler {
    private const val PERIODIC_USAGE_WORK = "anchor_periodic_usage_sync"
    private const val PERIODIC_POLICY_WORK = "anchor_periodic_policy_sync"
    private const val APP_LAUNCH_SYNC_WORK = "anchor_app_launch_sync"

    fun schedulePeriodic(context: Context) {
        ConnectivityState.init(context.applicationContext)
        val wm = WorkManager.getInstance(context.applicationContext)
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val usagePeriodic = PeriodicWorkRequestBuilder<UsageBatchUploadWorker>(15, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
            .build()
        wm.enqueueUniquePeriodicWork(
            PERIODIC_USAGE_WORK,
            ExistingPeriodicWorkPolicy.KEEP,
            usagePeriodic
        )

        val policyPeriodic = PeriodicWorkRequestBuilder<PolicyDownloadWorker>(30, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
            .build()
        wm.enqueueUniquePeriodicWork(
            PERIODIC_POLICY_WORK,
            ExistingPeriodicWorkPolicy.KEEP,
            policyPeriodic
        )
    }

    fun syncNow(context: Context) {
        val app = context.applicationContext
        ConnectivityState.init(app)
        if (!SyncPreferences(app).isConfiguredForDeviceSync()) return
        if (!ConnectivityState.isOnline(app)) return

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val usage = OneTimeWorkRequestBuilder<UsageBatchUploadWorker>()
            .setConstraints(constraints)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
            .build()
        val policy = OneTimeWorkRequestBuilder<PolicyDownloadWorker>()
            .setConstraints(constraints)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
            .build()
        WorkManager.getInstance(app)
            .beginUniqueWork(APP_LAUNCH_SYNC_WORK, ExistingWorkPolicy.REPLACE, policy)
            .then(usage)
            .enqueue()
    }

    fun enqueueUsageUpload(context: Context) {
        val app = context.applicationContext
        ConnectivityState.init(app)
        if (!ConnectivityState.isOnline(app)) return
        UsageBatchUploadWorker.enqueue(app)
    }

    fun enqueuePolicyDownload(context: Context) {
        val app = context.applicationContext
        ConnectivityState.init(app)
        if (!ConnectivityState.isOnline(app)) return
        PolicyDownloadWorker.enqueue(app)
    }
}
