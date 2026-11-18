package com.anchorguard.client.network

import android.content.Context
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import java.net.SocketTimeoutException

/**
 * Connectivity-aware gating and exception tagging for unified networking.
 * Throws IOException variants that map cleanly to NetworkError.
 */
class NetworkErrorInterceptor(context: Context) : Interceptor {
    private val appContext = context.applicationContext

    override fun intercept(chain: Interceptor.Chain): Response {
        if (!ConnectivityState.isOnline(appContext)) {
            throw OfflineIOException("Device is offline")
        }
        return try {
            chain.proceed(chain.request())
        } catch (t: SocketTimeoutException) {
            throw t
        } catch (t: IOException) {
            // Re-check connectivity for transient network failures
            if (!ConnectivityState.isOnline(appContext)) throw OfflineIOException("Device is offline", t)
            throw t
        }
    }
}

class OfflineIOException(message: String, cause: Throwable? = null) : IOException(message, cause)
