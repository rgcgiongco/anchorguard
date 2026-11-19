package com.anchorguard.client

import android.app.Application
import com.anchorguard.client.data.location.LocationManager
import com.anchorguard.client.data.location.LocationTrackingController
import com.anchorguard.client.data.sync.SyncScheduler
import com.anchorguard.client.network.ConnectivityState
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class AnchorGuardApp : Application() {
    override fun onCreate() {
        super.onCreate()
        ConnectivityState.init(this)
        SyncScheduler.schedulePeriodic(this)
        LocationManager.init(this)
        LocationTrackingController.init(this)
        LocationRepository.init(this)
    }
}
