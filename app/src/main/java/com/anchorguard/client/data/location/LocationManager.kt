package com.anchorguard.client.data.location

import android.Manifest.permission
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices

class LocationManager private constructor(
    private val context: Context,
    private val fusedLocationClient: FusedLocationProviderClient
) {

    private var locationCallback: LocationCallback? = null
    private var currentLocation: Location? = null

    fun requestLocationUpdates(
        activity: Activity,
        intervalMillis: Long = 5 * 60 * 1000,
        fastestIntervalMillis: Long = 60 * 1000,
        onLocation: (Location) -> Unit
    ): Boolean {
        if (!hasFineLocationPermission()) return false
        val request = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
            interval = intervalMillis
            fastestInterval = fastestIntervalMillis
        }
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { loc ->
                    currentLocation = loc
                    onLocation(loc)
                }
            }
        }
        fusedLocationClient.requestLocationUpdates(request, locationCallback, Looper.getMainLooper())
        return true
    }

    suspend fun getLastLocation(): Location? = currentLocation

    fun getLastLocationSync(): Location? = currentLocation

    fun removeLocationUpdates() {
        locationCallback?.let { fusedLocationClient.removeLocationUpdates(it) }
        locationCallback = null
    }

    fun hasFineLocationPermission(): Boolean =
        ContextCompat.checkSelfPermission(context, permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

    fun hasCoarseLocationPermission(): Boolean =
        ContextCompat.checkSelfPermission(context, permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

    fun hasBackgroundLocationPermission(): Boolean =
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            ContextCompat.checkSelfPermission(context, permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED
        } else true

    fun shutdown() {
        removeLocationUpdates()
        currentLocation = null
    }

    companion object {
        @Volatile private var instance: LocationManager? = null

        fun init(context: Context): LocationManager {
            return instance ?: synchronized(this) {
                instance ?: LocationManager(
                    context.applicationContext,
                    LocationServices.getFusedLocationProviderClient(context.applicationContext)
                ).also { instance = it }
            }
        }

        fun instance(): LocationManager = instance ?: error("LocationManager not initialized. Call init() first.")
    }
}
