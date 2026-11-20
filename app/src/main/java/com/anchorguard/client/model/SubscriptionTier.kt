package com.anchorguard.client.model

/**
 * Subscription tier for the AnchorGuard app.
 * FREE tier enforces minimal limits: 1 device, social media blocking only, manual limits, no email support.
 * PAID tier unlocks all features.
 */
enum class SubscriptionTier {
    FREE, PAID
}

/**
 * User tier configuration with feature flags and limits.
 */
data class UserTier(
    val tier: SubscriptionTier,
    val deviceLimit: Int,
    val allowedCategories: List<String>,
    val allowSchedules: Boolean,
    val emailSupport: Boolean
)

/**
 * Default FREE tier configuration.
 */
fun defaultFreeTier(): UserTier = UserTier(
    tier = SubscriptionTier.FREE,
    deviceLimit = 1,
    allowedCategories = listOf("social_media"),
    allowSchedules = false,
    emailSupport = false
)

/**
 * Default PAID tier configuration (unlimited).
 */
fun defaultPaidTier(): UserTier = UserTier(
    tier = SubscriptionTier.PAID,
    deviceLimit = Int.MAX_VALUE,
    allowedCategories = listOf("social_media", "chat", "gaming", "learning"),
    allowSchedules = true,
    emailSupport = true
)
