package com.anchorguard.client.enforcement

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log

class AppWatchdog(
    context: Context,
    private val onTick: (String?) -> Unit
) {
    private val appContext = context.applicationContext
    private val handler = Handler(Looper.getMainLooper())
    private val policy = PolicyEngine(appContext)
    private val usageTracker = AppUsageTracker(appContext)

    @Volatile
    private var running = false

    private val runnable = object : Runnable {
        override fun run() {
            if (!running) return
            try {
                val pkg = usageTracker.currentForegroundPackage()
                EnforcementController.updateForegroundPackage(pkg)
                onTick(pkg)
            } catch (t: Throwable) {
                Log.w(TAG, "Watchdog tick failed", t)
            } finally {
                if (running) {
                    handler.postDelayed(this, policy.blockIntervalMs())
                }
            }
        }
    }

    fun start() {
        if (running) return
        running = true
        handler.post(runnable)
    }

    fun stop() {
        running = false
        handler.removeCallbacks(runnable)
    }

    fun hasUsageAccess(): Boolean = usageTracker.hasUsageAccess()

    companion object {
        private const val TAG = "AnchorGuardWatchdog"
    }
}
