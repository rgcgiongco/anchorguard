package com.anchorguard.client.enforcement

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.anchorguard.client.R
import com.anchorguard.client.auth.data.AuthRepository
import com.anchorguard.client.data.local.AnchorGuardDatabase
import com.anchorguard.client.data.local.dao.GeofenceDao
import com.anchorguard.client.data.local.entity.Geofence
import com.anchorguard.client.data.sync.NetworkConfig
import com.anchorguard.client.data.sync.SyncApi
import com.anchorguard.client.data.sync.SyncPreferences
import com.anchorguard.client.data.sync.GeofenceZoneDto
import com.anchorguard.client.network.NetworkModule
import com.anchorguard.client.network.NetworkResult
import com.anchorguard.client.network.safeApiCall
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GeofenceManager private constructor(
    private val appContext: Context,
    private val scope: CoroutineScope
) {

    private val db = AnchorGuardDatabase.getInstance(appContext)
    private val geofenceDao: GeofenceDao = db.geofenceDao()
    private val syncPrefs = SyncPreferences(appContext)
    private val authRepo = AuthRepository(appContext)

    private var currentZoneIds = mutableSetOf<String>()
    private var geofenceJob: Job? = null

    fun start() {
        stop()
        downloadGeofences()
    }

    fun stop() {
        geofenceJob?.cancel()
        geofenceJob = null
    }

    private fun downloadGeofences() {
        val deviceId = syncPrefs.deviceId() ?: return
        geofenceJob = scope.launch {
            try {
                val result = withContext(Dispatchers.Default) {
                    val api: SyncApi = NetworkModule.syncApi(appContext)
                    api.downloadGeofences(deviceId)
                }
                when (result) {
                    is NetworkResult.Success -> {
                        val zones = result.data
                        if (zones.isNotEmpty()) {
                            withContext(Dispatchers.Default) {
                                geofenceDao.deleteForDevice(deviceId)
                                val entities = zones.map { dto ->
                                    Geofence(
                                        id = dto.id,
                                        deviceId = deviceId,
                                        zoneId = dto.id,
                                        name = dto.name,
                                        latitude = dto.latitude,
                                        longitude = dto.longitude,
                                        radiusMeters = dto.radiusMeters,
                                        synced = true
                                    )
                                }
                                geofenceDao.insertAll(entities)
                            }
                            Log.i(TAG, "Downloaded ${zones.size} geofences")
                        }
                    }
                    is NetworkResult.Failure -> {
                        Log.w(TAG, "Failed to download geofences: ${result.error.userMessage()}")
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Geofence download error", e)
            }
        }
    }

    /**
     * Call this whenever a new location fix arrives. Checks against all stored geofences
     * and generates ENTER/EXIT alerts for zone transitions.
     */
    fun onLocationUpdate(latitude: Double, longitude: Double, timestampMillis: Long) {
        val zones = geofenceDao.getAllForDevice(syncPrefs.deviceId() ?: "")
        if (zones.isEmpty()) return

        val nowInside = zones.filter { zone ->
            haversineMeters(latitude, longitude, zone.latitude, zone.longitude) <= zone.radiusMeters.toDouble()
        }.map { it.zoneId }.toSet()

        // Detect enters
        for (zoneId in nowInside - currentZoneIds) {
            val zone = zones.find { it.zoneId == zoneId }
            if (zone != null) {
                Log.i(TAG, "Geofence ENTER: ${zone.name}")
                postGeofenceAlert(zone.zoneId, zone.name, "ENTER", latitude, longitude, timestampMillis)
            }
        }

        // Detect exits
        for (zoneId in currentZoneIds - nowInside) {
            val zone = zones.find { it.zoneId == zoneId }
            if (zone != null) {
                Log.i(TAG, "Geofence EXIT: ${zone.name}")
                postGeofenceAlert(zone.zoneId, zone.name, "EXIT", latitude, longitude, timestampMillis)
            }
        }

        currentZoneIds = nowInside
    }

    private fun postGeofenceAlert(
        zoneId: String,
        zoneName: String,
        alertType: String,
        latitude: Double,
        longitude: Double,
        timestampMillis: Long
    ) {
        scope.launch {
            try {
                withContext(Dispatchers.Default) {
                    showGeofenceNotification(zoneName, alertType)
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed to post geofence alert", e)
            }
        }
    }

    private fun showGeofenceNotification(zoneName: String, alertType: String) {
        try {
            val channelId = "anchor_geofence_alerts"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val mgr = appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                mgr.createNotificationChannel(
                    NotificationChannel(channelId, "Geofence Alerts", NotificationManager.IMPORTANCE_HIGH)
                )
            }
            val title = when (alertType) {
                "ENTER" -> "Entered $zoneName"
                "EXIT" -> "Left $zoneName"
                else -> "Geofence alert: $zoneName"
            }
            val pi = android.app.PendingIntent.getActivity(
                appContext,
                0,
                android.content.Intent(appContext, com.anchorguard.client.MainActivity::class.java).apply {
                    addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP)
                },
                android.app.PendingIntent.FLAG_IMMUTABLE
            )
            val builder = NotificationCompat.Builder(appContext, channelId)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText("Geofence alert for ${alertType.toLowerCase()}")
                .setContentIntent(pi)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
            val mgr = appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            mgr.notify(zoneName.hashCode(), builder.build())
        } catch (t: Throwable) {
            Log.w(TAG, "Failed to show geofence notification", t)
        }
    }

    /** Haversine great-circle distance in meters. */
    private fun haversineMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val R = 6_371_000.0
        val phi1 = Math.toRadians(lat1)
        val phi2 = Math.toRadians(lat2)
        val deltaPhi = Math.toRadians(lat2 - lat1)
        val deltaLambda = Math.toRadians(lon2 - lon1)
        val a = Math.sin(deltaPhi / 2) * Math.sin(deltaPhi / 2) +
                Math.cos(phi1) * Math.cos(phi2) * Math.sin(deltaLambda / 2) * Math.sin(deltaLambda / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1.0 - a))
        return R * c
    }

    companion object {
        private const val TAG = "GeofenceManager"
        private var instance: GeofenceManager? = null

        fun init(context: Context): GeofenceManager {
            return synchronized(this) {
                instance ?: GeofenceManager(
                    context.applicationContext,
                    CoroutineScope(Dispatchers.Main.immediate)
                ).also { instance = it }
            }
        }

        fun getInstance(): GeofenceManager? = instance
    }
}
