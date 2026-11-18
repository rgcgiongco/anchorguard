package com.anchorguard.client.network

import android.content.Context
import com.anchorguard.client.auth.security.SecureSessionStore
import com.anchorguard.client.data.sync.SyncPreferences

/**
 * Unified access to networking configuration and credentials.
 * Base URL and device credentials are primarily mirrored in SyncPreferences for existing sync config;
 * auth tokens are sourced from secure storage.
 */
class NetworkConfig(context: Context) {
    private val appContext = context.applicationContext
    private val session = SecureSessionStore(appContext)
    private val syncPrefs = SyncPreferences(appContext)

    fun baseUrl(): String = syncPrefs.baseUrl().trim().ifEmpty { SyncPreferences.DEFAULT_BASE_URL }

    fun accessToken(): String? = session.parentAccessToken() ?: syncPrefs.accessToken()?.takeIf { it.isNotBlank() }

    fun refreshToken(): String? = session.parentRefreshToken()

    fun deviceKey(): String? = session.deviceApiKey() ?: syncPrefs.deviceKey()

    fun deviceId(): String? = session.deviceId() ?: syncPrefs.deviceId()

    fun isDeviceProvisioned(): Boolean = !deviceId().isNullOrBlank()

    fun clearAuthSession() {
        session.clearSession(clearPin = false)
        // SecureSessionStore already mirrors token clear to sync prefs
    }
}
