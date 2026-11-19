package com.anchorguard.client.auth.security

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.anchorguard.client.data.sync.SyncPreferences

/**
 * Encrypted storage for parent auth/session and device credentials.
 * Access/refresh tokens are stored here (not in plain SharedPreferences).
 */
class SecureSessionStore(context: Context) {
    private val appContext = context.applicationContext
    private val prefs: SharedPreferences = try {
        val masterKey = MasterKey.Builder(appContext)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            appContext,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    } catch (_: Throwable) {
        // Fallback only to avoid hard crash on unsupported keystore states; production should remain encrypted.
        appContext.getSharedPreferences(PREFS_NAME_FALLBACK, Context.MODE_PRIVATE)
    }

    private val syncPrefs = SyncPreferences(appContext)

    fun setParentTokens(accessToken: String?, refreshToken: String?) {
        prefs.edit()
            .putString(KEY_PARENT_ACCESS, accessToken)
            .putString(KEY_PARENT_REFRESH, refreshToken)
            .apply()
        // Keep sync workers functional by mirroring access token to sync prefs (used for authenticated sync if needed).
        syncPrefs.setAccessToken(accessToken)
    }

    fun parentAccessToken(): String? = prefs.getString(KEY_PARENT_ACCESS, null)?.takeIf { it.isNotBlank() }
    fun parentRefreshToken(): String? = prefs.getString(KEY_PARENT_REFRESH, null)?.takeIf { it.isNotBlank() }

    fun setDeviceCredentials(deviceId: String?, deviceApiKey: String?, paired: Boolean) {
        prefs.edit()
            .putString(KEY_DEVICE_ID, deviceId)
            .putString(KEY_DEVICE_API_KEY, deviceApiKey)
            .putBoolean(KEY_DEVICE_PAIRED, paired)
            .apply()
        // Mirror to sync preferences for existing sync workers.
        syncPrefs.setDeviceId(deviceId)
        syncPrefs.setDeviceKey(deviceApiKey)
    }

    fun deviceId(): String? = prefs.getString(KEY_DEVICE_ID, null)?.takeIf { it.isNotBlank() }
    fun deviceApiKey(): String? = prefs.getString(KEY_DEVICE_API_KEY, null)?.takeIf { it.isNotBlank() }
    fun isDevicePaired(): Boolean = prefs.getBoolean(KEY_DEVICE_PAIRED, false) && !deviceId().isNullOrBlank()

    fun setParentEmail(email: String?) {
        prefs.edit().putString(KEY_PARENT_EMAIL, email).apply()
    }

    fun parentEmail(): String? = prefs.getString(KEY_PARENT_EMAIL, null)

    fun clearSession(clearPin: Boolean = false) {
        val pinKeep = if (clearPin) null else prefs.all.filterKeys { it.startsWith("pin_") || it.startsWith("lockout_") }
        prefs.edit().clear().apply()
        if (!clearPin && !pinKeep.isNullOrEmpty()) {
            val e = prefs.edit()
            pinKeep.forEach { (k, v) ->
                when (v) {
                    is String -> e.putString(k, v)
                    is Boolean -> e.putBoolean(k, v)
                    is Int -> e.putInt(k, v)
                    is Long -> e.putLong(k, v)
                }
            }
            e.apply()
        }
        syncPrefs.setAccessToken(null)
    }

    fun clearAll() {
        prefs.edit().clear().apply()
        syncPrefs.setAccessToken(null)
        syncPrefs.setDeviceId(null)
        syncPrefs.setDeviceKey(null)
    }

    internal fun raw(): SharedPreferences = prefs

    companion object {
        private const val PREFS_NAME = "anchor_secure_session"
        private const val PREFS_NAME_FALLBACK = "anchor_secure_session_fallback"
        private const val KEY_PARENT_ACCESS = "parent_access_token"
        private const val KEY_PARENT_REFRESH = "parent_refresh_token"
        private const val KEY_DEVICE_ID = "device_id"
        private const val KEY_DEVICE_API_KEY = "device_api_key"
        private const val KEY_DEVICE_PAIRED = "device_paired"
    private const val KEY_PARENT_EMAIL = "parent_email"
    private const val KEY_SETUP_WIZARD_COMPLETED = "setup_wizard_completed"
    private const val KEY_DEVICE_ADMIN_GRANTED = "device_admin_granted"
    private const val KEY_ACCESSIBILITY_GRANTED = "accessibility_granted"
    private const val KEY_OVERLAY_GRANTED = "overlay_granted"
    private const val KEY_BATTERY_OPT_OUT = "battery_opt_out"
    private const val KEY_WIZARD_ROLE = "wizard_role"
    private const val KEY_SOS_ENABLED = "sos_enabled"
    private const val KEY_GEOFENCE_ENABLED = "geofence_enabled"
    }

    fun setupWizardCompleted(): Boolean = prefs.getBoolean(KEY_SETUP_WIZARD_COMPLETED, false)

    fun setSetupWizardCompleted(completed: Boolean) {
        prefs.edit().putBoolean(KEY_SETUP_WIZARD_COMPLETED, completed).apply()
    }

    fun setWizardRole(role: String) {
        prefs.edit().putString(KEY_WIZARD_ROLE, role).apply()
    }

    fun wizardRole(): String? = prefs.getString(KEY_WIZARD_ROLE, null)?.takeIf { it.isNotBlank() }

    fun isParentRole(): Boolean = wizardRole() == "parent"
    fun isChildRole(): Boolean = wizardRole() == "child"

    fun setDeviceAdminGranted(granted: Boolean) {
        prefs.edit().putBoolean(KEY_DEVICE_ADMIN_GRANTED, granted).apply()
    }

    fun setAccessibilityGranted(granted: Boolean) {
        prefs.edit().putBoolean(KEY_ACCESSIBILITY_GRANTED, granted).apply()
    }

    fun setOverlayGranted(granted: Boolean) {
        prefs.edit().putBoolean(KEY_OVERLAY_GRANTED, granted).apply()
    }

    fun setBatteryOptOut(granted: Boolean) {
        prefs.edit().putBoolean(KEY_BATTERY_OPT_OUT, granted).apply()
    }

    fun isDeviceAdminGranted(): Boolean = prefs.getBoolean(KEY_DEVICE_ADMIN_GRANTED, false)
    fun isAccessibilityGranted(): Boolean = prefs.getBoolean(KEY_ACCESSIBILITY_GRANTED, false)
    fun isOverlayGranted(): Boolean = prefs.getBoolean(KEY_OVERLAY_GRANTED, false)
    fun isBatteryOptOut(): Boolean = prefs.getBoolean(KEY_BATTERY_OPT_OUT, false)

    fun allWizardPermissionsGranted(): Boolean =
        isDeviceAdminGranted() && isAccessibilityGranted() && isOverlayGranted() && isBatteryOptOut()

    fun setSosEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_SOS_ENABLED, enabled).apply()
    }

    fun isSosEnabled(): Boolean = prefs.getBoolean(KEY_SOS_ENABLED, false)

    fun setGeofenceEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_GEOFENCE_ENABLED, enabled).apply()
    }

    fun isGeofenceEnabled(): Boolean = prefs.getBoolean(KEY_GEOFENCE_ENABLED, false)
}
