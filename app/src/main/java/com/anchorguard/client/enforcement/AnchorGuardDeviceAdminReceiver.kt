package com.anchorguard.client.enforcement

import android.app.admin.DeviceAdminReceiver
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.anchorguard.client.MainActivity
import com.anchorguard.client.R
import com.anchorguard.client.auth.security.PinSecurityManager

class AnchorGuardDeviceAdminReceiver : DeviceAdminReceiver() {
    override fun onEnabled(context: Context, intent: Intent) {
        super.onEnabled(context, intent)
        Log.i(TAG, "Device admin enabled")
        Toast.makeText(context, "AnchorGuard device admin enabled", Toast.LENGTH_SHORT).show()
        if (PinSecurityManager(context.applicationContext).isPinSet()) {
            requestParentVerification(context, "Device admin enabled. Verify parent PIN.")
        }
    }

    override fun onDisabled(context: Context, intent: Intent) {
        super.onDisabled(context, intent)
        Log.w(TAG, "Device admin disabled — enforcement will be inactive")
        Toast.makeText(context, "AnchorGuard device admin disabled — app blocking will not work", Toast.LENGTH_LONG).show()
        if (PinSecurityManager(context.applicationContext).isPinSet()) {
            requestParentVerification(context, "Device admin disabled. Parent verification required to re-enable.")
        }
        showAdminDisabledNotification(context.applicationContext)
    }

    override fun onPasswordChanged(context: Context, intent: Intent, userHandle: android.os.UserHandle) {
        super.onPasswordChanged(context, intent, userHandle)
        Log.i(TAG, "Device password changed")
    }

    override fun onLockTaskModeEntering(context: Context, intent: Intent, pkg: String) {
        super.onLockTaskModeEntering(context, intent, pkg)
        Log.i(TAG, "Lock task mode entering: $pkg")
    }

    override fun onDisableRequested(context: Context, intent: Intent): CharSequence {
        val appContext = context.applicationContext
        val pinManager = PinSecurityManager(appContext)
        val dpm = appContext.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val admin = ComponentName(appContext, AnchorGuardDeviceAdminReceiver::class.java)
        val isActive = dpm.isAdminActive(admin)
        Log.i(TAG, "Device admin disable requested (active=$isActive)")
        if (pinManager.isPinSet()) {
            pinManager.clearParentSession()
            requestParentVerification(appContext, "Parent PIN required before changing device admin protection.")
            return "AnchorGuard protection is active. Verify the parent PIN in AnchorGuard before disabling device admin."
        }
        return "Disabling AnchorGuard device admin will stop app blocking and remote lock. Continue?"
    }

    private fun requestParentVerification(context: Context, reason: String) {
        try {
            val i = Intent(context, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                putExtra(BlockOverlayView.EXTRA_OPEN_PARENT_GATE, true)
                putExtra(EXTRA_ADMIN_VERIFY_REASON, reason)
            }
            context.startActivity(i)
        } catch (t: Throwable) {
            Log.w(TAG, "Failed to request parent verification", t)
        }
    }

    private fun showAdminDisabledNotification(context: Context) {
        try {
            val channelId = "anchor_admin_disabled"
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
                manager.createNotificationChannel(
                    android.app.NotificationChannel(channelId, "AnchorGuard Admin", android.app.NotificationManager.IMPORTANCE_HIGH)
                )
            }
            val pi = android.app.PendingIntent.getActivity(
                context,
                0,
                Intent(context, MainActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                },
                android.app.PendingIntent.FLAG_IMMUTABLE
            )
            val builder = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("AnchorGuard protection disabled")
                .setContentText("Device admin is off. App blocking and remote lock will not work until re-enabled.")
                .setContentIntent(pi)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
            manager.notify(1002, builder.build())
        } catch (t: Throwable) {
            Log.w(TAG, "Failed to show admin disabled notification", t)
        }
    }

    companion object {
        private const val TAG = "AnchorGuardAdmin"
        const val EXTRA_ADMIN_VERIFY_REASON = "com.anchorguard.client.extra.ADMIN_VERIFY_REASON"
    }
}
