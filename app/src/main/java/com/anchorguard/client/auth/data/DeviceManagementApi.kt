package com.anchorguard.client.auth.data

import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Body

/**
 * Parent-level device management endpoints (device list, create, rename, remove).
 * Requires parent Bearer auth via AuthInterceptor.
 */
interface DeviceManagementApi {
    @GET("devices")
    suspend fun listDevices(): Response<List<DeviceResponse>>

    @POST("devices")
    suspend fun createChildDevice(@Body body: DeviceCreateRequest): Response<DeviceResponse>

    @PATCH("devices/{deviceId}")
    suspend fun renameDevice(
        @Path("deviceId") deviceId: String,
        @Body body: DevicePatchRequest
    ): Response<DeviceDetailResponse>

    @DELETE("devices/{deviceId}")
    suspend fun removeDevice(@Path("deviceId") deviceId: String): Response<MessageResponse>
}
