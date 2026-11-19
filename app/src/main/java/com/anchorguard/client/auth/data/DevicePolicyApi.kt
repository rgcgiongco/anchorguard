package com.anchorguard.client.auth.data

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

/**
 * Device policy endpoints: app allow/block list, schedules, per-app limits, remote lock, SOS.
 * Authorization and X-Device-Key headers are injected by unified network interceptors.
 */
interface DevicePolicyApi {
    @GET("devices/{deviceId}/policy")
    suspend fun getPolicy(@Path("deviceId") deviceId: String): Response<DevicePolicyResponse>

    @PATCH("devices/{deviceId}/policy")
    suspend fun patchPolicy(
        @Path("deviceId") deviceId: String,
        @Body body: PolicyPatchRequest
    ): Response<DevicePolicyResponse>

    @GET("devices/{deviceId}/apps")
    suspend fun getApps(@Path("deviceId") deviceId: String): Response<List<AppPolicyResponse>>

    @PATCH("devices/{deviceId}/apps/{packageName}")
    suspend fun patchApp(
        @Path("deviceId") deviceId: String,
        @Path("packageName") packageName: String,
        @Body body: AppPolicyPatchRequest
    ): Response<AppPolicyResponse>

    @GET("devices/{deviceId}/schedule")
    suspend fun getSchedule(@Path("deviceId") deviceId: String): Response<ScheduleResponse>

    @PUT("devices/{deviceId}/schedule")
    suspend fun setSchedule(
        @Path("deviceId") deviceId: String,
        @Body body: ScheduleUpdateRequest
    ): Response<ScheduleResponse>

    @POST("devices/{deviceId}/lock")
    suspend fun lockDevice(
        @Path("deviceId") deviceId: String,
        @Body body: LockRequest? = null
    ): Response<MessageResponse>

    @POST("devices/{deviceId}/unlock")
    suspend fun unlockDevice(
        @Path("deviceId") deviceId: String
    ): Response<MessageResponse>

    @GET("devices/{deviceId}/status")
    suspend fun getDeviceStatus(@Path("deviceId") deviceId: String): Response<DeviceStatusResponse>

    @POST("devices/{deviceId}/sos")
    suspend fun sendSos(
        @Path("deviceId") deviceId: String,
        @Body body: SosEventRequest
    ): Response<SosEventResponse>
}
