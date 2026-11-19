package com.anchorguard.client.data.location

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

class LocationPermissionHelper private constructor(private val activity: FragmentActivity) {

    private const val REQUEST_CODE = 1002

    fun requestLocationPermissions(onResult: (granted: Boolean) -> Unit) {
        val perms = arrayOf(
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        )
        val missing = perms.filter {
            ContextCompat.checkSelfPermission(activity, it) != PackageManager.PERMISSION_GRANTED
        }
        if (missing.isEmpty()) {
            onResult(true)
            return
        }
        val rationaleNeeded = missing.any { ActivityCompat.shouldShowRequestPermissionRationale(activity, it) }
        if (rationaleNeeded) {
            onResult(false)
            return
        }
        ActivityCompat.requestPermissions(activity, perms, REQUEST_CODE)
        onResult(false)
    }

    fun onRequestPermissionsResult(requestCode: Int, grantResults: IntArray, onResult: (granted: Boolean) -> Unit) {
        if (requestCode != REQUEST_CODE) return
        val granted = grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }
        onResult(granted)
    }

    fun isBackgroundLocationGranted(): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContextCompat.checkSelfPermission(activity, android.Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED
        } else true

    fun requestBackgroundLocation() {
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(android.Manifest.permission.ACCESS_BACKGROUND_LOCATION),
            REQUEST_CODE + 1
        )
    }

    companion object {
        @Volatile private var instance: LocationPermissionHelper? = null

        fun init(activity: FragmentActivity): LocationPermissionHelper {
            return instance ?: synchronized(this) {
                instance ?: LocationPermissionHelper(activity).also { instance = it }
            }
        }

        fun instance(): LocationPermissionHelper = instance ?: error("LocationPermissionHelper not initialized.")
    }
}
