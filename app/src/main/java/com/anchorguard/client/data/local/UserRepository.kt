package com.anchorguard.client.data.local

import android.content.Context
import com.anchorguard.client.auth.security.SecureSessionStore
import com.anchorguard.client.model.SubscriptionTier
import com.anchorguard.client.model.UserTier
import com.anchorguard.client.model.defaultFreeTier
import com.anchorguard.client.model.defaultPaidTier

/**
 * Repository for user subscription tier and feature gating.
 * Reads tier from SecureSessionStore (populated from login response) and falls back to FREE tier.
 */
class UserRepository(private val context: Context) {
    private val session = SecureSessionStore(context)

    /**
     * Returns the user's current tier configuration.
     * Defaults to FREE tier if no tier is stored.
     */
    fun getUserTier(): UserTier {
        val tierStr = session.userTier() ?: return defaultFreeTier()
        val tier = SubscriptionTier.valueOf(tierStr.uppercase())
        return when (tier) {
            SubscriptionTier.FREE -> defaultFreeTier()
            SubscriptionTier.PAID -> defaultPaidTier()
        }
    }

    /**
     * Checks whether a specific feature is allowed for the current tier.
     */
    fun isFeatureAllowed(feature: String): Boolean {
        val tier = getUserTier()
        return when (feature) {
            "schedules" -> tier.allowSchedules
            "email_support" -> tier.emailSupport
            "social_media_blocking" -> "social_media" in tier.allowedCategories
            "chat_blocking" -> "chat" in tier.allowedCategories
            "gaming_blocking" -> "gaming" in tier.allowedCategories
            "learning_blocking" -> "learning" in tier.allowedCategories
            else -> false
        }
    }

    /**
     * Returns the maximum number of linked devices allowed for the current tier.
     */
    fun getDeviceLimit(): Int = getUserTier().deviceLimit

    /**
     * Stores the user tier (typically called after login).
     */
    fun setUserTier(tier: String?) {
        session.setUserTier(tier)
    }
}
