package com.anchorguard.client.enforcement

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.anchorguard.client.MainActivity
import com.anchorguard.client.R

class AnchorGuardService : Service() {

    private lateinit var policyEngine: PolicyEngine
    private var watchdog: AppWatchdog? = null
    private var screenStateReceiver: ScreenStateReceiver? = null

    override fun onCreate() {
        super.onCreate()
        policyEngine = PolicyEngine(applicationContext)
        createNotificationChannel()
        watchdog = AppWatchdog(applicationContext) { pkg -> evaluate(pkg) }
        registerScreenReceiver()
        Log.i(TAG, "Service created")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                transitionToIdle()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
                return START_NOT_STICKY
            }
            ACTION_EVALUATE -> {
                evaluate(intent.getStringExtra(EXTRA_PACKAGE) ?: EnforcementController.foregroundPackage.value)
            }
            ACTION_REFRESH -> {
                evaluate(EnforcementController.foregroundPackage.value)
            }
            else -> {
                startForeground(NOTIFICATION_ID, buildNotification())
                if (EnforcementController.state.value == EnforcementState.IDLE) {
                    EnforcementController.updateState(EnforcementState.RUNNING)
                }
                watchdog?.start()
                evaluate(EnforcementController.foregroundPackage.value)
            }
        }
        return START_REDELIVER_INTENT
    }

    private fun evaluate(pkg: String?) {
        val policy = policyEngine
        if (policy.isParentMode()) {
            BlockOverlayView.hide(applicationContext)
            EnforcementController.updateState(EnforcementState.PARENT_MODE)
            return
        }
        val blocked = !pkg.isNullOrBlank() && !policy.isAllowed(pkg)
        if (blocked) {
            EnforcementController.updateState(EnforcementState.BLOCKED)
            BlockOverlayView.show(applicationContext, pkg)
        } else {
            BlockOverlayView.hide(applicationContext)
            if (EnforcementController.state.value != EnforcementState.IDLE) {
                EnforcementController.updateState(EnforcementState.RUNNING)
            }
        }
    }

    private fun registerScreenReceiver() {
        if (screenStateReceiver != null) return
        val receiver = ScreenStateReceiver()
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Intent.ACTION_USER_PRESENT)
        }
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                registerReceiver(receiver, filter, RECEIVER_NOT_EXPORTED)
            } else {
                @Suppress("UnspecifiedRegisterReceiverFlag")
                registerReceiver(receiver, filter)
            }
            screenStateReceiver = receiver
        } catch (t: Throwable) {
            Log.w(TAG, "Failed to register screen receiver", t)
        }
    }

    private fun unregisterScreenReceiver() {
        val receiver = screenStateReceiver ?: return
        try {
            unregisterReceiver(receiver)
        } catch (_: Throwable) {
        } finally {
            screenStateReceiver = null
        }
    }

    private fun transitionToIdle() {
        watchdog?.stop()
        BlockOverlayView.hide(applicationContext)
        EnforcementController.updateState(EnforcementState.IDLE)
    }

    override fun onDestroy() {
        transitionToIdle()
        unregisterScreenReceiver()
        watchdog = null
        Log.i(TAG, "Service destroyed")
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val mgr = getSystemService(NotificationManager::class.java)
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.enforcement_channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.enforcement_channel_description)
                setSound(null, null)
                enableVibration(false)
                setShowBadge(false)
            }
            mgr.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification {
        val pi = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(getString(R.string.enforcement_notification_title))
            .setContentText(getString(R.string.enforcement_notification_text))
            .setContentIntent(pi)
            .setOngoing(true)
            .setSilent(true)
            .setOnlyAlertOnce(true)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    companion object {
        private const val TAG = "AnchorGuardService"
        private const val CHANNEL_ID = "anchor_guard_protection"
        private const val NOTIFICATION_ID = 1001
        private const val ACTION_STOP = "com.anchorguard.client.action.STOP_ENFORCEMENT"
        private const val ACTION_EVALUATE = "com.anchorguard.client.action.EVALUATE_ENFORCEMENT"
        private const val ACTION_REFRESH = "com.anchorguard.client.action.REFRESH_ENFORCEMENT"
        private const val EXTRA_PACKAGE = "extra_package"

        fun start(context: Context) {
            val intent = Intent(context, AnchorGuardService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, AnchorGuardService::class.java).apply { action = ACTION_STOP }
            try { context.startService(intent) } catch (_: Throwable) {}
        }

        fun requestEvaluate(context: Context, packageName: String?) {
            val intent = Intent(context, AnchorGuardService::class.java).apply {
                action = ACTION_EVALUATE
                putExtra(EXTRA_PACKAGE, packageName)
            }
            try { context.startService(intent) } catch (_: Throwable) {}
        }

        fun requestRefresh(context: Context) {
            val intent = Intent(context, AnchorGuardService::class.java).apply { action = ACTION_REFRESH }
            try { context.startService(intent) } catch (_: Throwable) {}
        }
    }
}
