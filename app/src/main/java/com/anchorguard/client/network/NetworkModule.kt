package com.anchorguard.client.network

import android.content.Context
import com.anchorguard.client.BuildConfig
import com.anchorguard.client.auth.data.AuthApi
import com.anchorguard.client.auth.data.AuthTokensResponse
import com.anchorguard.client.auth.data.RefreshRequest
import com.anchorguard.client.data.sync.SyncApi
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

/**
 * Unified Retrofit/OkHttp networking module (Module 07).
 * Single cached OkHttpClient + Retrofit instance for AuthApi and SyncApi.
 */
object NetworkModule {
    @Volatile private var cachedBaseUrl: String? = null
    @Volatile private var cachedClient: OkHttpClient? = null
    @Volatile private var cachedRetrofit: Retrofit? = null
    @Volatile private var cachedAuthApi: AuthApi? = null
    @Volatile private var cachedSyncApi: SyncApi? = null

    private interface RefreshAuthApi {
        @POST("auth/refresh")
        fun refresh(@Body body: RefreshRequest): Call<AuthTokensResponse>
    }

    fun authApi(context: Context): AuthApi {
        ensure(context)
        return cachedAuthApi!!
    }

    fun syncApi(context: Context): SyncApi {
        ensure(context)
        return cachedSyncApi!!
    }

    fun retrofit(context: Context): Retrofit {
        ensure(context)
        return cachedRetrofit!!
    }

    fun okHttpClient(context: Context): OkHttpClient {
        ensure(context)
        return cachedClient!!
    }

    @Synchronized
    private fun ensure(context: Context) {
        val app = context.applicationContext
        val config = NetworkConfig(app)
        val baseUrl = normalize(config.baseUrl())
        if (cachedRetrofit != null && cachedBaseUrl == baseUrl) return

        // Separate minimal client for token refresh to avoid authenticator recursion
        val refreshClient = OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(DeviceKeyInterceptor(app))
            .addInterceptor(loggingInterceptor())
            .build()
        val refreshRetrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(refreshClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val refreshApi = refreshRetrofit.create(RefreshAuthApi::class.java)

        val client = OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(NetworkErrorInterceptor(app))
            .addInterceptor(DeviceKeyInterceptor(app))
            .addInterceptor(AuthInterceptor(app))
            .addInterceptor(loggingInterceptor())
            .authenticator(SessionAuthenticator(app) { { req -> refreshApi.refresh(req) } })
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        cachedBaseUrl = baseUrl
        cachedClient = client
        cachedRetrofit = retrofit
        cachedAuthApi = retrofit.create(AuthApi::class.java)
        cachedSyncApi = retrofit.create(SyncApi::class.java)
    }

    fun invalidate() {
        synchronized(this) {
            cachedBaseUrl = null
            cachedClient = null
            cachedRetrofit = null
            cachedAuthApi = null
            cachedSyncApi = null
        }
    }

    private fun normalize(url: String): String {
        val trimmed = url.trim().ifEmpty { "https://api.anchorguard.com/v1/" }
        return if (trimmed.endsWith("/")) trimmed else "$trimmed/"
    }

    private fun loggingInterceptor(): HttpLoggingInterceptor = HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BASIC else HttpLoggingInterceptor.Level.NONE
    }
}
