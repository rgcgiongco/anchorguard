package com.anchorguard.client.data.location

import android.content.Context
import com.anchorguard.client.data.local.AnchorGuardDatabase
import com.anchorguard.client.data.local.dao.DeviceSettingDao
import com.anchorguard.client.data.local.entity.DeviceSetting

class LocationTrackingController private constructor(
    private val context: Context,
    private val deviceSettingDao: DeviceSettingDao
) {

    suspend fun isTrackingEnabled(): Boolean {
        val setting = deviceSettingDao.get(context.packageName, KEY_LOCATION_TRACKING)
        return setting?.value == "true"
    }

    suspend fun setTrackingEnabled(enabled: Boolean) {
        val setting = DeviceSetting(
            id = "${context.packageName}:$KEY_LOCATION_TRACKING",
            deviceId = context.packageName,
            key = KEY_LOCATION_TRACKING,
            value = if (enabled) "true" else "false",
            updatedAt = System.currentTimeMillis()
        )
        deviceSettingDao.set(setting)
        if (enabled) {
            if (LocationManager.instance().hasFineLocationPermission()) {
                LocationForegroundService.start(context)
            }
        } else {
            LocationForegroundService.stop(context)
        }
    }

    suspend fun toggleTracking() {
        setTrackingEnabled(!isTrackingEnabled())
    }

    companion object {
        private const val KEY_LOCATION_TRACKING = "location_tracking_enabled"

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
