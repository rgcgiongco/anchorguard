package com.anchorguard.client.data.sync

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * Sync endpoints. Bearer token and X-Device-Key are injected by unified network interceptors.
 */
interface SyncApi {
    @POST("usage/batch")
    suspend fun uploadUsageBatch(
        @Body body: UsageBatchRequest
    ): Response<UsageBatchResponse>

    @GET("policies")
    suspend fun getPolicies(
        @Query("deviceId") deviceId: String?,
        @Query("since") since: Long? = null
    ): Response<PoliciesResponse>
}
