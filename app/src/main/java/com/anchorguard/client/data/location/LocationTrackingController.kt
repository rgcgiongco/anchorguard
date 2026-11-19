package com.anchorguard.client.data.location

import android.content.Context
import android.content.Intent
import android.os.Build
import com.anchorguard.client.data.local.AnchorGuardDatabase
import com.anchorguard.client.data.local.dao.DeviceSettingDao
import com.anchorguard.client.data.local.entity.DeviceSetting
import kotlin.concurrent.thread

class LocationTrackingController private constructor(
    private val context: Context,
    private val deviceSettingDao: DeviceSettingDao
) {

    private const val KEY_LOCATION_TRACKING = "location_tracking_enabled"

    suspend fun isTrackingEnabled(): Boolean {
        val setting = deviceSettingDao.get(context.packageName, KEY_LOCATION_TRACKING)
        return setting?.value == "true"
    }

    fun setTrackingEnabled(enabled: Boolean) {
        val setting = DeviceSetting(
            id = "${context.packageName}:$KEY_LOCATION_TRACKING",
            deviceId = context.packageName,
            key = KEY_LOCATION_TRACKING,
            value = if (enabled) "true" else "false",
            updatedAt = System.currentTimeMillis()
        )
        thread(isDaemon = true) {
            deviceSettingDao.set(setting)
            if (enabled) {
                if (LocationManager.instance().hasFineLocationPermission()) {
                    LocationForegroundService.start(context)
                }
            } else {
                LocationForegroundService.stop(context)
            }
        }
    }

    fun toggleTracking() {
        setTrackingEnabled(!isTrackingEnabled())
    }

    companion object {
        @Volatile private var instance: LocationTrackingController? = null

        fun init(context: Context): LocationTrackingController {
            val app = context.applicationContext
            return instance ?: synchronized(this) {
                instance ?: LocationTrackingController(
                    app,
                    AnchorGuardDatabase.getInstance(app).deviceSettingDao()
                ).also { instance = it }
            }
        }

        fun instance(): LocationTrackingController = instance ?: error("LocationTrackingController not initialized.")
    }
}
