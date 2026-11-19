package com.anchorguard.client.data.sync

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object LocationSyncScheduler {
    private const val PERIODIC_LOCATION_SYNC = "anchor_periodic_location_sync"

    fun schedulePeriodic(context: Context) {
        val wm = WorkManager.getInstance(context.applicationContext)
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val periodic = PeriodicWorkRequestBuilder<LocationSyncWorker>(15, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
            .build()
        wm.enqueueUniquePeriodicWork(
            PERIODIC_LOCATION_SYNC,
            ExistingPeriodicWorkPolicy.KEEP,
            periodic
        )
    }

    fun enqueueImmediate(context: Context) {
        LocationSyncWorker.enqueue(context.applicationContext)
    }

    fun cancelPeriodic(context: Context) {
        WorkManager.getInstance(context.applicationContext)
            .cancelUniqueWork(PERIODIC_LOCATION_SYNC)
    }
}
