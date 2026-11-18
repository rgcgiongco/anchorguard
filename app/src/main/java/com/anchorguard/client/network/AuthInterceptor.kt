package com.anchorguard.client.network

import android.content.Context
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Injects parent bearer token for authenticated API calls when not already provided.
 */
class AuthInterceptor(context: Context) : Interceptor {
    private val config = NetworkConfig(context.applicationContext)

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val builder = original.newBuilder()

        if (original.header("Authorization").isNullOrBlank()) {
            val token = config.accessToken()
            if (!token.isNullOrBlank()) {
                builder.header("Authorization", "Bearer $token")
            }
        }

        return chain.proceed(builder.build())
    }
}
