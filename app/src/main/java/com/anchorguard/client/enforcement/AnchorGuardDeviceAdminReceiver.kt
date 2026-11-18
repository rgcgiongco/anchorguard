package com.anchorguard.client.enforcement

import android.app.admin.DeviceAdminReceiver
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.anchorguard.client.MainActivity
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
        Log.i(TAG, "Device admin disabled")
        Toast.makeText(context, "AnchorGuard device admin disabled", Toast.LENGTH_SHORT).show()
        if (PinSecurityManager(context.applicationContext).isPinSet()) {
            requestParentVerification(context, "Device admin disabled. Parent verification required.")
        }
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
        return "Disabling AnchorGuard device admin may reduce protection on this device."
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

    companion object {
        private const val TAG = "AnchorGuardAdmin"
        const val EXTRA_ADMIN_VERIFY_REASON = "com.anchorguard.client.extra.ADMIN_VERIFY_REASON"
    }
}
