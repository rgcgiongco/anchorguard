package com.anchorguard.client.auth.data

import android.content.Context
import android.os.Build
import com.anchorguard.client.BuildConfig
import com.anchorguard.client.auth.security.SecureSessionStore
import com.anchorguard.client.data.sync.SyncPreferences
import com.anchorguard.client.network.NetworkModule
import com.anchorguard.client.network.NetworkResult
import com.anchorguard.client.network.NetworkError
import com.anchorguard.client.network.safeApiCall
import java.util.Locale

class AuthRepository(context: Context) {
    private val appContext = context.applicationContext
    private val session = SecureSessionStore(appContext)
    private val syncPrefs = SyncPreferences(appContext)

    private fun api(): AuthApi = NetworkModule.authApi(appContext)

    suspend fun register(email: String, password: String, name: String?): NetworkResult<AuthTokensResponse> {
        val result = safeApiCall(appContext) {
            api().register(RegisterRequest(email.trim().lowercase(Locale.ROOT), password, name?.trim()?.takeIf { it.isNotEmpty() }))
        }
        if (result is NetworkResult.Success) {
            val body = result.data
            session.setParentTokens(body.accessToken, body.refreshToken)
            session.setParentEmail(email.trim().lowercase(Locale.ROOT))
        }
        return result
    }

    suspend fun login(email: String, password: String): NetworkResult<AuthTokensResponse> {
        val result = safeApiCall(appContext) {
            api().login(LoginRequest(email.trim().lowercase(Locale.ROOT), password))
        }
        if (result is NetworkResult.Success) {
            val body = result.data
            session.setParentTokens(body.accessToken, body.refreshToken)
            session.setParentEmail(email.trim().lowercase(Locale.ROOT))
        }
        return result
    }

    suspend fun logout(): NetworkResult<Unit> {
        val rt = session.parentRefreshToken()
        val result = if (rt != null) {
            safeApiCall(appContext) { api().logout(LogoutRequest(rt)) }
        } else {
            NetworkResult.Success(Unit)
        }
        session.setParentTokens(null, null)
        session.setParentEmail(null)
        return when (result) {
            is NetworkResult.Success -> NetworkResult.Success(Unit)
            is NetworkResult.Failure -> {
                // best effort logout; local session is cleared regardless
                NetworkResult.Success(Unit)
            }
        }
    }

    suspend fun refreshIfNeeded(): NetworkResult<String?> {
        val rt = session.parentRefreshToken() ?: return NetworkResult.Failure(NetworkError.Auth(401, "No refresh token"))
        val result = safeApiCall(appContext) { api().refresh(RefreshRequest(rt)) }
        return when (result) {
            is NetworkResult.Success -> {
                val body = result.data
                session.setParentTokens(body.accessToken, body.refreshToken)
                NetworkResult.Success(body.accessToken)
            }
            is NetworkResult.Failure -> {
                if (result.error is NetworkError.Auth) {
                    session.setParentTokens(null, null)
                }
                NetworkResult.Failure(result.error)
            }
        }
    }

    suspend fun registerDevice(): NetworkResult<DeviceRegisterResponse> {
        val req = DeviceRegisterRequest(
            model = Build.MODEL,
            manufacturer = Build.MANUFACTURER,
            androidId = null,
            appVersion = BuildConfig.VERSION_NAME
        )
        val result = safeApiCall(appContext) { api().registerDevice(req) }
        if (result is NetworkResult.Success) {
            val body = result.data
            session.setDeviceCredentials(body.deviceId, body.deviceApiKey, paired = false)
        }
        return result
    }

    suspend fun pairDevice(pairingCode: String): NetworkResult<DeviceResponse> {
        if (ensureAccessToken() == null) {
            return NetworkResult.Failure(NetworkError.Auth(401, "Sign in required to pair a device"))
        }
        val result = safeApiCall(appContext) {
            api().pairDevice(PairDeviceRequest(pairingCode.trim().uppercase(Locale.ROOT)))
        }
        return when (result) {
            is NetworkResult.Success -> {
                val body = result.data
                val existingId = session.deviceId()
                val existingKey = session.deviceApiKey()
                if (!existingId.isNullOrBlank() && existingId == body.id && !existingKey.isNullOrBlank()) {
                    session.setDeviceCredentials(existingId, existingKey, paired = true)
                } else {
                    session.raw().edit().putBoolean("last_pair_success", true).apply()
                }
                result
            }
            is NetworkResult.Failure -> result
        }
    }

    suspend fun ensureAccessToken(): String? {
        val current = session.parentAccessToken()
        if (!current.isNullOrBlank()) return current
        return when (val r = refreshIfNeeded()) {
            is NetworkResult.Success -> r.data
            else -> null
        }
    }

    fun hasParentSession(): Boolean = !session.parentAccessToken().isNullOrBlank() || !session.parentRefreshToken().isNullOrBlank()
    fun isDevicePaired(): Boolean = session.isDevicePaired()
    fun deviceId(): String? = session.deviceId()
    fun pairingPrefs(): SyncPreferences = syncPrefs
    fun secureSession(): SecureSessionStore = session
}
