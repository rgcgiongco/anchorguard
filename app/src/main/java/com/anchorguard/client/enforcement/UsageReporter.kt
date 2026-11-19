package com.anchorguard.client.enforcement

import android.content.Context
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import com.anchorguard.client.data.local.AnchorGuardDatabase
import com.anchorguard.client.data.sync.SyncPreferences
import com.anchorguard.client.data.sync.UsageBatchUploadWorker
import com.anchorguard.client.network.NetworkConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UsageReporter private constructor(
    private val appContext: Context,
    private val scope: CoroutineScope
) : DefaultLifecycleObserver {

    private var currentPackage: String? = null
    private var currentStartMs: Long = 0
    private var reportJob: Job? = null

    private val db = AnchorGuardDatabase.getInstance(appContext)
    private val usageDao = db.usageEventDao()
    private val syncPrefs = SyncPreferences(appContext)

    override fun onStart(owner: androidx.lifecycle.LifecycleOwner) {
        super.onStart(owner)
        start()
    }

    override fun onStop(owner: androidx.lifecycle.LifecycleOwner) {
        super.onStop(owner)
        stop()
    }

    fun start() {
        stop()
        val deviceId = NetworkConfig(appContext).deviceId()
        if (deviceId == null) return

        reportJob = scope.launch {
            try {
                EnforcementController.foregroundPackage.collect { pkg ->
                    handleForegroundChange(pkg)
                }
            } catch (e: Exception) {
                Log.w(TAG, "Usage reporter foreground collection failed", e)
            }
        }
    }

    fun stop() {
        // Flush current app usage before stopping
        flushCurrent()
        reportJob?.cancel()
        reportJob = null
    }

    private fun handleForegroundChange(newPkg: String?) {
        val now = System.currentTimeMillis()

        // End previous app session
        if (currentPackage != null && currentPackage != newPkg && currentStartMs > 0) {
            val durationMs = now - currentStartMs
            if (durationMs >= MIN_RECORD_DURATION_MS) {
                scope.launch {
                    recordEvent(currentPackage!!, currentStartMs, now, durationMs)
                }
            }
        }

        // Start new app session
        currentPackage = newPkg
        currentStartMs = now
    }

    private fun flushCurrent() {
        if (currentPackage != null && currentStartMs > 0) {
            val now = System.currentTimeMillis()
            val durationMs = now - currentStartMs
            if (durationMs >= MIN_RECORD_DURATION_MS) {
                scope.launch {
                    recordEvent(currentPackage!!, currentStartMs, now, durationMs)
                }
            }
        }
        currentPackage = null
        currentStartMs = 0
    }

    private suspend fun recordEvent(packageName: String, startedAt: Long, endedAt: Long, durationMs: Long) {
        val deviceUserId = syncPrefs.deviceUserId() ?: "default"
        val event = com.anchorguard.client.data.local.entity.UsageEvent(
            id = java.util.UUID.randomUUID().toString(),
            deviceUserId = deviceUserId,
            packageName = packageName,
            eventType = "foreground",
            startedAt = startedAt,
            endedAt = endedAt,
            durationMs = durationMs,
            synced = false,
            retryCount = 0,
            deadLetter = false
        )
        try {
            withContext(Dispatchers.Default) {
                usageDao.insert(event)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to record usage event", e)
        }
    }

    fun triggerUpload() {
        val deviceId = syncPrefs.deviceId()
        if (deviceId == null) return
        UsageBatchUploadWorker.enqueue(appContext)
    }

    companion object {
        private const val TAG = "UsageReporter"
        private const val MIN_RECORD_DURATION_MS = 5_000L // 5 seconds minimum

        private var instance: UsageReporter? = null

        fun init(context: Context): UsageReporter {
            return synchronized(this) {
                instance ?: UsageReporter(
                    context.applicationContext,
                    CoroutineScope(Dispatchers.Main.immediate)
                ).also { instance = it }
            }
        }

        fun getInstance(): UsageReporter? = instance
    }
}
