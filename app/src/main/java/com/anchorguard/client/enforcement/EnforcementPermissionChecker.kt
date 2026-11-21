package com.anchorguard.client.enforcement

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.core.content.ContextCompat

class EnforcementPermissionChecker(context: Context) {
    private val appContext = context.applicationContext
    private val dpm: DevicePolicyManager =
        appContext.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    private val powerManager: PowerManager =
        appContext.getSystemService(Context.POWER_SERVICE) as PowerManager

    private val adminComponent = ComponentName(appContext, AnchorGuardDeviceAdminReceiver::class.java)

    fun isDeviceAdminActive(): Boolean = dpm.isAdminActive(adminComponent)

    fun isAccessibilityServiceEnabled(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2) return false
        val services = Settings.Secure.getString(
            appContext.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        )
        if (services.isNullOrBlank()) return false
        return services.split(":").any { it.contains(AnchorGuardAccessibilityService::class.java.name, ignoreCase = true) }
    }

    fun isOverlayPermissionGranted(): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) Settings.canDrawOverlays(appContext) else true

    fun isBatteryOptimizationExempt(): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) powerManager.isIgnoringBatteryOptimizations(appContext.packageName) else true

    fun isLocationPermissionGranted(): Boolean =
        ContextCompat.checkSelfPermission(appContext, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

    fun requestLocation() {
        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        appContext.startActivity(intent)
    }

    fun allPermissionsGranted(): Boolean =
        isDeviceAdminActive() && isAccessibilityServiceEnabled() && isOverlayPermissionGranted() && isBatteryOptimizationExempt()

    fun requestDeviceAdmin() {
        val intent = Intent(android.app.admin.DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
            putExtra(android.app.admin.DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminComponent)
            putExtra(android.app.admin.DevicePolicyManager.EXTRA_ADD_EXPLANATION, "AnchorGuard needs device admin to enforce app blocking and remote lock.")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        appContext.startActivity(intent)
    }

    fun requestAccessibility() {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        appContext.startActivity(intent)
    }

    fun requestOverlay() {
        val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply {
            data = Uri.parse("package:${appContext.packageName}")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        appContext.startActivity(intent)
    }

    fun requestBatteryOptimization() {
        val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
            data = Uri.parse("package:${appContext.packageName}")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        appContext.startActivity(intent)
    }
}
