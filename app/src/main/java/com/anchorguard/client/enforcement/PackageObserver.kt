package com.anchorguard.client.enforcement

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class PackageObserver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return
        val pkg = intent.data?.schemeSpecificPart ?: return
        if (pkg == context.packageName) return
        when (action) {
            Intent.ACTION_PACKAGE_ADDED,
            Intent.ACTION_PACKAGE_REPLACED,
            Intent.ACTION_PACKAGE_REMOVED,
            Intent.ACTION_PACKAGE_FULLY_REMOVED -> {
                Log.i(TAG, "Package change: $action $pkg")
                // MVP: refresh enforcement evaluation; policy sync/auto-rules can be added later.
                AnchorGuardService.requestRefresh(context)
            }
        }
    }

    companion object {
        private const val TAG = "AnchorGuardPackages"
    }
}
