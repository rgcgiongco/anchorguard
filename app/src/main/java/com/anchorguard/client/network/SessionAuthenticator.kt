package com.anchorguard.client.network

import android.content.Context
import com.anchorguard.client.auth.data.AuthTokensResponse
import com.anchorguard.client.auth.data.RefreshRequest
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import retrofit2.Call

/**
 * Handles 401 responses by refreshing the access token once and retrying the request.
 * On refresh failure, clears local auth session (logout).
 */
class SessionAuthenticator(
    context: Context,
    private val refreshCallProvider: () -> (RefreshRequest) -> Call<AuthTokensResponse>
) : Authenticator {
    private val appContext = context.applicationContext
    private val config = NetworkConfig(appContext)

    override fun authenticate(route: Route?, response: Response): Request? {
        if (responseCount(response) >= 2) {
            config.clearAuthSession()
            return null
        }

        val path = response.request.url.encodedPath
        // Do not try to refresh for auth endpoints (login/register/refresh/logout)
        if (path.contains("/auth/login") || path.contains("/auth/register") ||
            path.contains("/auth/refresh") || path.contains("/auth/logout") ||
            path.contains("/auth/password-reset")
        ) {
            return null
        }

        val refreshToken = config.refreshToken() ?: run {
            config.clearAuthSession()
            return null
        }

        synchronized(this) {
            val latest = config.accessToken()
            val requestAuth = response.request.header("Authorization")?.removePrefix("Bearer ")?.trim()
            // If token changed since this request was made, retry with latest token
            if (!latest.isNullOrBlank() && latest != requestAuth) {
                return response.request.newBuilder()
                    .header("Authorization", "Bearer $latest")
                    .build()
            }

            val newToken = try {
                val refreshResponse = refreshCallProvider()(RefreshRequest(refreshToken)).execute()
                if (refreshResponse.isSuccessful) {
                    val body = refreshResponse.body()
                    val access = body?.accessToken
                    val newRefresh = body?.refreshToken ?: refreshToken
                    if (!access.isNullOrBlank()) {
                        val store = com.anchorguard.client.auth.security.SecureSessionStore(appContext)
                        store.setParentTokens(access, newRefresh)
                        store.setUserTier(body.tier)
                        access
                    } else null
                } else {
                    null
                }
            } catch (_: Throwable) {
                null
            }

            if (newToken.isNullOrBlank()) {
                config.clearAuthSession()
                return null
            }

            return response.request.newBuilder()
                .header("Authorization", "Bearer $newToken")
                .build()
        }
    }

    private fun responseCount(response: Response): Int {
        var result = 1
        var prior = response.priorResponse
        while (prior != null) {
            result++
            prior = prior.priorResponse
        }
        return result
    }
}
