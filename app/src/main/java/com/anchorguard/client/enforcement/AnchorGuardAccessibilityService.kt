package com.anchorguard.client.enforcement

import android.accessibilityservice.AccessibilityService
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.core.app.NotificationCompat
import com.anchorguard.client.MainActivity
import com.anchorguard.client.R

class AnchorGuardAccessibilityService : AccessibilityService() {
    override fun onAccessibilityEvent(event: android.view.accessibility.AccessibilityEvent?) {
        if (event == null) return
        when (event.eventType) {
            android.view.accessibility.AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED,
            android.view.accessibility.AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED -> {
                val pkg = event.packageName?.toString()
                if (!pkg.isNullOrBlank()) {
                    EnforcementController.updateForegroundPackage(pkg)
                    AnchorGuardService.requestEvaluate(this, pkg)
                }
            }
        }
    }

    override fun onInterrupt() {
        Log.i(TAG, "Accessibility service interrupted")
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.i(TAG, "Accessibility service connected")
    }

    override fun onUnbind(intent: android.content.Intent?): Boolean {
        Log.w(TAG, "Accessibility service unbound — app blocking may not work")
        showAccessibilityDisabledNotification(applicationContext)
        // Attempt to keep enforcement service running
        AnchorGuardService.requestRefresh(applicationContext)
        return super.onUnbind(intent)
    }

    private fun showAccessibilityDisabledNotification(context: Context) {
        try {
            val channelId = "anchor_accessibility_disabled"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
                manager.createNotificationChannel(
                    android.app.NotificationChannel(channelId, "AnchorGuard Accessibility", android.app.NotificationManager.IMPORTANCE_HIGH)
                )
            }
            val pi = PendingIntent.getActivity(
                context,
                0,
                Intent(context, MainActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                },
                PendingIntent.FLAG_IMMUTABLE
            )
            val builder = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("AnchorGuard accessibility service disabled")
                .setContentText("App blocking will not work. Re-enable Accessibility in Settings.")
                .setContentIntent(pi)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
            manager.notify(1005, builder.build())
        } catch (t: Throwable) {
            Log.w(TAG, "Failed to show accessibility disabled notification", t)
        }
    }

    companion object {
        private const val TAG = "AnchorGuardA11y"
    }
}
