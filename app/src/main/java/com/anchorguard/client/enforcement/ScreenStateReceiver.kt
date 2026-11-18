package com.anchorguard.client.enforcement

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class ScreenStateReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        when (intent?.action) {
            Intent.ACTION_SCREEN_OFF -> {
                Log.i(TAG, "Screen off")
                BlockOverlayView.hide(context.applicationContext)
            }
            Intent.ACTION_SCREEN_ON, Intent.ACTION_USER_PRESENT -> {
                Log.i(TAG, "Screen on/unlocked: ${intent.action}")
                AnchorGuardService.requestRefresh(context)
            }
        }
    }

    companion object {
        private const val TAG = "AnchorGuardScreen"
    }
}
