package com.anchorguard.client.auth.data

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Parent auth and device pairing endpoints.
 * Authorization and X-Device-Key headers are injected by unified network interceptors where applicable.
 */
interface AuthApi {
    @POST("auth/register")
    suspend fun register(@Body body: RegisterRequest): Response<AuthTokensResponse>

    @POST("auth/login")
    suspend fun login(@Body body: LoginRequest): Response<AuthTokensResponse>

    @POST("auth/refresh")
    suspend fun refresh(@Body body: RefreshRequest): Response<AuthTokensResponse>

    @POST("auth/logout")
    suspend fun logout(@Body body: LogoutRequest): Response<MessageResponse>

    @POST("devices/register")
    suspend fun registerDevice(
        @Body body: DeviceRegisterRequest = DeviceRegisterRequest()
    ): Response<DeviceRegisterResponse>

    @POST("devices/pair")
    suspend fun pairDevice(
        @Body body: PairDeviceRequest
    ): Response<DeviceResponse>
}
