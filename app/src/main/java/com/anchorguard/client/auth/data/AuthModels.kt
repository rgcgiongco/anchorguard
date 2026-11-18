package com.anchorguard.client.auth.data

data class AuthTokensResponse(val accessToken: String, val refreshToken: String, val tokenType: String = "Bearer")
data class LoginRequest(val email: String, val password: String)
data class RegisterRequest(val email: String, val password: String, val name: String? = null)
data class RefreshRequest(val refreshToken: String)
data class LogoutRequest(val refreshToken: String)

data class ParentResponse(
    val id: String,
    val email: String,
    val name: String?,
    val emailVerified: Boolean,
    val createdAt: String
)

data class DeviceRegisterRequest(
    val name: String? = null,
    val model: String? = null,
    val manufacturer: String? = null,
    val androidId: String? = null,
    val appVersion: String? = null
)

data class DeviceRegisterResponse(
    val deviceId: String,
    val hexId: String,
    val pairingCode: String,
    val pairingExpiresAt: String,
    val deviceApiKey: String
)

data class PairDeviceRequest(val pairingCode: String)

data class DeviceResponse(
    val id: String,
    val hexId: String,
    val name: String?,
    val model: String?,
    val manufacturer: String?,
    val appVersion: String?,
    val lastSeen: String?,
    val isOnline: Boolean,
    val isPaired: Boolean,
    val createdAt: String
)

data class MessageResponse(val message: String)
