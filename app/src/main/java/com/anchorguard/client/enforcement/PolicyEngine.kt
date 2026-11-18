package com.anchorguard.client.enforcement

import android.content.Context
import android.content.SharedPreferences

class PolicyEngine(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun isAllowed(packageName: String): Boolean {
        if (packageName.isBlank()) return true
        if (packageName == OWN_PACKAGE) return true
        if (packageName == "com.android.systemui") return true
        if (prefs.getBoolean(KEY_PARENT_MODE, false)) return true
        val blocked = prefs.getStringSet(KEY_BLOCKED_PACKAGES, emptySet()).orEmpty()
        return packageName !in blocked
    }

    fun setBlockedPackages(packages: Set<String>) {
        prefs.edit().putStringSet(KEY_BLOCKED_PACKAGES, packages.toSet()).apply()
    }

    fun blockedPackages(): Set<String> = prefs.getStringSet(KEY_BLOCKED_PACKAGES, emptySet()).orEmpty()

    fun setParentMode(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_PARENT_MODE, enabled).apply()
    }

    fun isParentMode(): Boolean = prefs.getBoolean(KEY_PARENT_MODE, false)

    fun setBlockIntervalMs(ms: Long) {
        prefs.edit().putLong(KEY_BLOCK_INTERVAL_MS, ms.coerceAtLeast(MIN_BLOCK_INTERVAL_MS)).apply()
    }

    fun blockIntervalMs(): Long =
        prefs.getLong(KEY_BLOCK_INTERVAL_MS, DEFAULT_BLOCK_INTERVAL_MS).coerceAtLeast(MIN_BLOCK_INTERVAL_MS)

    companion object {
        private const val PREFS_NAME = "anchor_enforcement_policy"
        private const val KEY_BLOCKED_PACKAGES = "blocked_packages"
        private const val KEY_PARENT_MODE = "parent_mode"
        private const val KEY_BLOCK_INTERVAL_MS = "block_interval_ms"
        private const val OWN_PACKAGE = "com.anchorguard.client"
        const val DEFAULT_BLOCK_INTERVAL_MS = 500L
        const val MIN_BLOCK_INTERVAL_MS = 100L
    }
}
