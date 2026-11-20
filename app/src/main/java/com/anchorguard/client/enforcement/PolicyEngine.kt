package com.anchorguard.client.enforcement

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import java.util.Calendar

class PolicyEngine(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private var subscriptionTier: String? = null

    fun setSubscriptionTier(tier: String?) {
        subscriptionTier = tier
    }

    private fun isFreeTier(): Boolean = subscriptionTier == "FREE"

    fun isAllowed(packageName: String): Boolean {
        if (packageName.isBlank()) return true
        if (packageName == OWN_PACKAGE) return true
        if (packageName == "com.android.systemui") return true
        if (prefs.getBoolean(KEY_PARENT_MODE, false)) return true
        val blocked = prefs.getStringSet(KEY_BLOCKED_PACKAGES, emptySet()).orEmpty()
        if (packageName in blocked) return false
        // Free tier: ignore schedule-based enforcement (manual limits only)
        if (!isFreeTier() && !isWithinAllowedHours(packageName)) return false
        if (!isWithinDailyLimit(packageName)) return false
        return true
    }

    fun setBlockedPackages(packages: Set<String>) {
        prefs.edit().putStringSet(KEY_BLOCKED_PACKAGES, packages.toSet()).apply()
    }

    fun syncFromAppPolicies(blockedPackageNames: Set<String>) {
        setBlockedPackages(blockedPackageNames)
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

    /* ---------- Weekly schedule ---------- */

    fun setWeeklySchedule(json: String?) {
        if (json == null) {
            prefs.edit().remove(KEY_WEEKLY_SCHEDULE).apply()
        } else {
            prefs.edit().putString(KEY_WEEKLY_SCHEDULE, json).apply()
        }
    }

    fun getWeeklySchedule(): String? = prefs.getString(KEY_WEEKLY_SCHEDULE, null)

    fun isScheduleEnabled(): Boolean = getWeeklySchedule() != null

    private fun isWithinAllowedHours(packageName: String): Boolean {
        val json = getWeeklySchedule() ?: return true
        try {
            val obj = JsonParser.parseString(json).asJsonObject
            val now = Calendar.getInstance()
            val dayOfWeek = now.get(Calendar.DAY_OF_WEEK)
            val dayKey = when (dayOfWeek) {
                Calendar.MONDAY -> "monday"
                Calendar.TUESDAY -> "tuesday"
                Calendar.WEDNESDAY -> "wednesday"
                Calendar.THURSDAY -> "thursday"
                Calendar.FRIDAY -> "friday"
                Calendar.SATURDAY -> "saturday"
                Calendar.SUNDAY -> "sunday"
                else -> return true
            }
            val dayObj = obj.getAsJsonObject(dayKey) ?: return true
            val enabled = dayObj.get("enabled").asBoolean
            if (!enabled) return false
            val startHour = dayObj.get("startHour").asInt
            val startMinute = dayObj.get("startMinute").asInt
            val endHour = dayObj.get("endHour").asInt
            val endMinute = dayObj.get("endMinute").asInt
            val currentMinute = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)
            val startMinuteOfDay = startHour * 60 + startMinute
            val endMinuteOfDay = endHour * 60 + endMinute
            return currentMinute in startMinuteOfDay..endMinuteOfDay
        } catch (e: Exception) {
            return true
        }
    }

    /* ---------- Per-app daily limits ---------- */

    fun setAppDailyLimit(packageName: String, minutes: Int?) {
        val editor = prefs.edit()
        if (minutes == null) {
            editor.remove("$KEY_APP_DAILY_LIMIT_$packageName")
        } else {
            editor.putInt("$KEY_APP_DAILY_LIMIT_$packageName", minutes)
        }
        editor.apply()
    }

    fun getAppDailyLimit(packageName: String): Int? {
        val key = "$KEY_APP_DAILY_LIMIT_$packageName"
        return if (prefs.contains(key)) prefs.getInt(key, -1) else null
    }

    fun setAppUsageToday(packageName: String, minutes: Int) {
        prefs.edit().putInt("$KEY_APP_USAGE_TODAY_$packageName", minutes).apply()
    }

    fun getAppUsageToday(packageName: String): Int {
        val key = "$KEY_APP_USAGE_TODAY_$packageName"
        return if (prefs.contains(key)) prefs.getInt(key, 0) else 0
    }

    private fun isWithinDailyLimit(packageName: String): Boolean {
        val limit = getAppDailyLimit(packageName) ?: return true
        if (limit <= 0) return true
        val used = getAppUsageToday(packageName)
        return used < limit
    }

    companion object {
        private const val PREFS_NAME = "anchor_enforcement_policy"
        private const val KEY_BLOCKED_PACKAGES = "blocked_packages"
        private const val KEY_PARENT_MODE = "parent_mode"
        private const val KEY_BLOCK_INTERVAL_MS = "block_interval_ms"
        private const val KEY_WEEKLY_SCHEDULE = "weekly_schedule"
        private const val KEY_APP_DAILY_LIMIT_ = "app_daily_limit_"
        private const val KEY_APP_USAGE_TODAY_ = "app_usage_today_"
        private const val OWN_PACKAGE = "com.anchorguard.client"
        const val DEFAULT_BLOCK_INTERVAL_MS = 500L
        const val MIN_BLOCK_INTERVAL_MS = 100L
    }
}
