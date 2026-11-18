package com.anchorguard.client.network

import android.content.Context
import okhttp3.Interceptor
import okhttp3.Response

class DeviceKeyInterceptor(context: Context) : Interceptor {
    private val config = NetworkConfig(context.applicationContext)

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val builder = original.newBuilder()

        if (original.header("X-Device-Key").isNullOrBlank()) {
            config.deviceKey()?.let { builder.header("X-Device-Key", it) }
        }

        return chain.proceed(builder.build())
    }
}
