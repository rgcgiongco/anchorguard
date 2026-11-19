package com.anchorguard.client.enforcement

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import com.anchorguard.client.R

class BatteryOptimizationHelper(context: Context) {
    private val appContext = context.applicationContext
    private val powerManager: PowerManager =
        appContext.getSystemService(Context.POWER_SERVICE) as PowerManager

    fun isBatteryOptimizationExempt(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return true
        return powerManager.isIgnoringBatteryOptimizations(appContext.packageName)
    }

    fun requestBatteryOptimizationExemption() {
        val intent = Intent(android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
            data = Uri.parse("package:${appContext.packageName}")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        appContext.startActivity(intent)
    }

    fun showBatteryOptimizationWarning() {
        try {
            val channelId = "anchor_battery_optimization"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val manager = appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                manager.createNotificationChannel(
                    NotificationChannel(channelId, "AnchorGuard Battery", NotificationManager.IMPORTANCE_HIGH)
                )
            }
            val pi = android.app.PendingIntent.getActivity(
                appContext,
                0,
                Intent(appContext, com.anchorguard.client.MainActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                },
                android.app.PendingIntent.FLAG_IMMUTABLE
            )
            val builder = NotificationCompat.Builder(appContext, channelId)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("AnchorGuard battery optimization active")
                .setContentText("AnchorGuard may be killed by the system. Exclude from battery optimization for reliable enforcement.")
                .setContentIntent(pi)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
            val manager = appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.notify(1003, builder.build())
        } catch (t: Throwable) {
            // Notification failure is non-fatal
        }
    }
}
