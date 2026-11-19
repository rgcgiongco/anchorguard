package com.anchorguard.client.enforcement

import android.app.admin.DevicePolicyManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log

class LockCommandReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return
        if (action != ACTION_REMOTE_LOCK) return

        Log.i(TAG, "Received remote lock command")
        val appCtx = context.applicationContext
        val dpm = appCtx.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val admin = ComponentName(appCtx, AnchorGuardDeviceAdminReceiver::class.java)

        if (dpm.isAdminActive(admin)) {
            dpm.lockNow()
            Log.i(TAG, "Device locked via DevicePolicyManager.lockNow()")
        } else {
            Log.w(TAG, "Device admin not active — cannot lock device")
        }

        // Show the lock activity
        val lockIntent = Intent(appCtx, DeviceLockActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra(EXTRA_LOCK_REASON, intent.getStringExtra(EXTRA_LOCK_REASON) ?: "Device locked by parent")
        }
        appCtx.startActivity(lockIntent)
    }

    companion object {
        private const val TAG = "AnchorGuardLock"
        const val ACTION_REMOTE_LOCK = "com.anchorguard.client.action.REMOTE_LOCK"
        const val ACTION_REMOTE_UNLOCK = "com.anchorguard.client.action.REMOTE_UNLOCK"
        const val EXTRA_LOCK_REASON = "com.anchorguard.client.extra.LOCK_REASON"
    }
}
