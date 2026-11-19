package com.anchorguard.client.enforcement

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.SystemClock
import android.provider.Settings
import android.util.Log

class BootCompletedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return
        if (action == Intent.ACTION_BOOT_COMPLETED ||
            action == Intent.ACTION_LOCKED_BOOT_COMPLETED ||
            action == ACTION_RESTART_ENFORCEMENT
        ) {
            Log.i(TAG, "Received $action, starting enforcement service")
            try {
                AnchorGuardService.start(context)
            } catch (t: Throwable) {
                Log.w(TAG, "Failed to start enforcement service on $action", t)
            }

            // Re-check critical permissions after boot
            checkBootPermissions(context)

            // Schedule periodic enforcement restart alarm
            scheduleEnforcementRestartAlarm(context)
        }
    }

    private fun checkBootPermissions(context: Context) {
        val appCtx = context.applicationContext

        // Check device admin
        val dpm = appCtx.getSystemService(Context.DEVICE_POLICY_SERVICE) as android.app.admin.DevicePolicyManager
        val admin = ComponentName(appCtx, AnchorGuardDeviceAdminReceiver::class.java)
        val adminActive = dpm.isAdminActive(admin)
        Log.i(TAG, "Boot check: device admin active=$adminActive")

        // Check overlay permission
        val overlayGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(appCtx)
        } else {
            true
        }
        Log.i(TAG, "Boot check: overlay granted=$overlayGranted")

        // Check accessibility service
        val accessibilityEnabled = isAccessibilityServiceEnabled(appCtx)
        Log.i(TAG, "Boot check: accessibility enabled=$accessibilityEnabled")
        if (!accessibilityEnabled) {
            showAccessibilityWarning(appCtx)
        }

        // Check battery optimization
        val batteryHelper = BatteryOptimizationHelper(appCtx)
        val batteryExempt = batteryHelper.isBatteryOptimizationExempt()
        Log.i(TAG, "Boot check: battery exempt=$batteryExempt")
        if (!batteryExempt) {
            batteryHelper.showBatteryOptimizationWarning()
        }
    }

    private fun isAccessibilityServiceEnabled(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2) return false
        val services = android.provider.Settings.Secure.getString(
            context.contentResolver,
            android.provider.Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        )
        return services != null && services.split(":").any {
            it.contains(AnchorGuardAccessibilityService::class.java.name, ignoreCase = true)
        }
    }

    private fun showAccessibilityWarning(context: Context) {
        try {
            val channelId = "anchor_accessibility"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
                manager.createNotificationChannel(
                    android.app.NotificationChannel(channelId, "AnchorGuard Accessibility", android.app.NotificationManager.IMPORTANCE_HIGH)
                )
            }
            val pi = android.app.PendingIntent.getActivity(
                context,
                0,
                Intent(context, com.anchorguard.client.MainActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                },
                android.app.PendingIntent.FLAG_IMMUTABLE
            )
            val builder = androidx.core.app.NotificationCompat.Builder(context, channelId)
                .setSmallIcon(com.anchorguard.client.R.mipmap.ic_launcher)
                .setContentTitle("AnchorGuard accessibility service")
                .setContentText("Accessibility service is not enabled. App blocking will not work.")
                .setContentIntent(pi)
                .setAutoCancel(true)
                .setPriority(androidx.core.app.NotificationCompat.PRIORITY_HIGH)
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
            manager.notify(1004, builder.build())
        } catch (t: Throwable) {
            // Notification failure is non-fatal
        }
    }

    private fun scheduleEnforcementRestartAlarm(context: Context) {
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, BootCompletedReceiver::class.java).apply {
                action = ACTION_RESTART_ENFORCEMENT
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                9999,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // Restart enforcement every 30 minutes if the service was killed
            val intervalMs = 30L * 60 * 1000
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.ELAPSED_REALTIME_WAKEUP,
                        SystemClock.elapsedRealtime() + intervalMs,
                        pendingIntent
                    )
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    SystemClock.elapsedRealtime() + intervalMs,
                    pendingIntent
                )
            } else {
                alarmManager.setRepeating(
                    AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    SystemClock.elapsedRealtime() + intervalMs,
                    intervalMs,
                    pendingIntent
                )
            }
        } catch (t: Throwable) {
            Log.w(TAG, "Failed to schedule enforcement restart alarm", t)
        }
    }

    companion object {
        private const val TAG = "AnchorGuardBoot"
        const val ACTION_RESTART_ENFORCEMENT = "com.anchorguard.client.action.RESTART_ENFORCEMENT"
    }
}
