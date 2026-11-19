package com.anchorguard.client.data.sync

import android.content.Context
import android.content.SharedPreferences

class SyncPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun baseUrl(): String = prefs.getString(KEY_BASE_URL, DEFAULT_BASE_URL) ?: DEFAULT_BASE_URL

    fun setBaseUrl(url: String) {
        prefs.edit().putString(KEY_BASE_URL, url.trim().ifEmpty { DEFAULT_BASE_URL }).apply()
    }

    fun deviceId(): String? = prefs.getString(KEY_DEVICE_ID, null)?.takeIf { it.isNotBlank() }

    fun setDeviceId(id: String?) {
        prefs.edit().putString(KEY_DEVICE_ID, id).apply()
    }

    fun deviceKey(): String? = prefs.getString(KEY_DEVICE_KEY, null)?.takeIf { it.isNotBlank() }

    fun setDeviceKey(key: String?) {
        prefs.edit().putString(KEY_DEVICE_KEY, key).apply()
    }

    fun deviceUserId(): String? = prefs.getString(KEY_DEVICE_USER_ID, null)?.takeIf { it.isNotBlank() }

    fun setDeviceUserId(id: String?) {
        prefs.edit().putString(KEY_DEVICE_USER_ID, id).apply()
    }

    fun accessToken(): String? = prefs.getString(KEY_ACCESS_TOKEN, null)?.takeIf { it.isNotBlank() }

    fun setAccessToken(token: String?) {
        prefs.edit().putString(KEY_ACCESS_TOKEN, token).apply()
    }

    fun lastPolicySyncAt(tableHint: String = "app_policies"): Long =
        prefs.getLong("$KEY_LAST_SYNC_PREFIX$tableHint", 0L)

    fun setLastPolicySyncAt(tableHint: String, at: Long) {
        prefs.edit().putLong("$KEY_LAST_SYNC_PREFIX$tableHint", at).apply()
    }

    fun isConfiguredForDeviceSync(): Boolean = !deviceId().isNullOrBlank()

    companion object {
        private const val PREFS_NAME = "anchor_sync_prefs"
        private const val KEY_BASE_URL = "base_url"
        private const val KEY_DEVICE_ID = "device_id"
        private const val KEY_DEVICE_KEY = "device_key"
        private const val KEY_DEVICE_USER_ID = "device_user_id"
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_LAST_SYNC_PREFIX = "last_sync_"
        const val DEFAULT_BASE_URL = "https://anchorguard.tail78464.ts.net/api/v1/"
        const val TABLE_DEVICE_USERS = "device_users"
        const val TABLE_APP_POLICIES = "app_policies"
        const val TABLE_DEVICE_SETTINGS = "device_settings"
        const val TABLE_USAGE_EVENTS = "usage_events"
    }
}
