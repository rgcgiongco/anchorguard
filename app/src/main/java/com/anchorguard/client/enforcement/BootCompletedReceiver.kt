package com.anchorguard.client.enforcement

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
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
        }
    }

    companion object {
        private const val TAG = "AnchorGuardBoot"
        const val ACTION_RESTART_ENFORCEMENT = "com.anchorguard.client.action.RESTART_ENFORCEMENT"
    }
}
