package com.anchorguard.client.ui.model

/**
 * Represents a linked child device in the parent dashboard.
 */
data class Device(
    val id: String,
    val name: String,
    val model: String,
    val isOnline: Boolean,
    val lastSeen: String?,
    val isPaired: Boolean,
    val enforcementActive: Boolean,
    val batteryLevel: Int? = null,
    val appCount: Int = 0
)
