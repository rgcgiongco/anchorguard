package com.anchorguard.client.data.location

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location as AndroidLocation
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.anchorguard.client.MainActivity
import com.anchorguard.client.R
import com.anchorguard.client.data.local.entity.Location
import com.anchorguard.client.data.local.dao.LocationDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LocationForegroundService : Service() {

    private var locationManager: LocationManager? = null
    private var scope: CoroutineScope? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        Log.i(TAG, "Location service created")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                stopTracking()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
                return START_NOT_STICKY
            }
            ACTION_START_TRACKING -> {
                startTracking()
                return START_STICKY
            }
            else -> {
                startTracking()
                return START_STICKY
            }
        }
    }

    private fun startTracking() {
        startForeground(NOTIFICATION_ID, buildNotification())
        locationManager = LocationManager.instance()
        scope = CoroutineScope(Dispatchers.Default)
        scope?.launch {
            try {
                locationManager?.requestLocationUpdates(
                    onLocation = { androidLoc ->
                        scope?.launch { persistLocation(androidLoc) }
                    }
                )
            } catch (e: Exception) {
                Log.w(TAG, "Failed to request location updates", e)
            }
        }
    }

    private fun stopTracking() {
        scope?.cancel()
        scope = null
        locationManager?.removeLocationUpdates()
        locationManager = null
    }

    private suspend fun persistLocation(androidLoc: AndroidLocation) = withContext(Dispatchers.Main) {
        try {
            val dao = LocationDaoProvider.dao(applicationContext)
            dao.insert(Location(
                id = java.util.UUID.randomUUID().toString(),
                deviceId = "",
                latitude = androidLoc.latitude,
                longitude = androidLoc.longitude,
                accuracyMeters = androidLoc.accuracy.toInt(),
                timestampMillis = androidLoc.time,
                provider = androidLoc.provider ?: "fused"
            ))
        } catch (e: Exception) {
            Log.w(TAG, "Failed to persist location", e)
        }
    }

    override fun onDestroy() {
        stopTracking()
        Log.i(TAG, "Location service destroyed")
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val mgr = getSystemService(NotificationManager::class.java)
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.location_channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.location_channel_description)
                setSound(null, null)
                enableVibration(false)
                setShowBadge(false)
            }
            mgr.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification {
        val pi = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(getString(R.string.location_notification_title))
            .setContentText(getString(R.string.location_notification_text))
            .setContentIntent(pi)
            .setOngoing(true)
            .setSilent(true)
            .setOnlyAlertOnce(true)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    companion object {
        private const val TAG = "LocationForegroundService"
        private const val CHANNEL_ID = "anchor_guard_location"
        private const val NOTIFICATION_ID = 1002
        private const val ACTION_START_TRACKING = "com.anchorguard.client.action.START_LOCATION_TRACKING"
        private const val ACTION_STOP = "com.anchorguard.client.action.STOP_LOCATION_TRACKING"

        fun start(context: Context) {
            val intent = Intent(context, LocationForegroundService::class.java).apply {
                action = ACTION_START_TRACKING
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, LocationForegroundService::class.java).apply {
                action = ACTION_STOP
            }
            try { context.startService(intent) } catch (_: Throwable) {}
        }
    }
}

object LocationDaoProvider {
    @Volatile private var dao: LocationDao? = null
    @Volatile private var lastContext: Context? = null

    fun dao(context: Context): LocationDao {
        val app = context.applicationContext
        if (dao != null && lastContext == app) return dao!!
        val db = com.anchorguard.client.data.local.AnchorGuardDatabase.getInstance(app)
        val d = db.locationDao()
        lastContext = app
        dao = d
        return d
    }
}
