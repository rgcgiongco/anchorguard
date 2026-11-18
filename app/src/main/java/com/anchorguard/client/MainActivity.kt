package com.anchorguard.client

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.mutableIntStateOf
import androidx.fragment.app.FragmentActivity
import com.anchorguard.client.data.sync.SyncScheduler
import com.anchorguard.client.enforcement.BlockOverlayView
import com.anchorguard.client.ui.nav.AnchorGuardNavHost
import com.anchorguard.client.ui.theme.AnchorGuardTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    private val openParentGateTick = mutableIntStateOf(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        handleIntent(intent)
        setContent {
            AnchorGuardTheme {
                AnchorGuardNavHost(openParentGateTick = openParentGateTick.intValue)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        SyncScheduler.syncNow(this)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent?.getBooleanExtra(BlockOverlayView.EXTRA_OPEN_PARENT_GATE, false) == true) {
            openParentGateTick.intValue += 1
            intent.removeExtra(BlockOverlayView.EXTRA_OPEN_PARENT_GATE)
        }
    }
}
