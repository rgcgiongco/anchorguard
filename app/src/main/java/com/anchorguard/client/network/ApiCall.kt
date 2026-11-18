package com.anchorguard.client.network

import android.content.Context
import retrofit2.Response

suspend fun <T> safeApiCall(
    context: Context,
    call: suspend () -> Response<T>
): NetworkResult<T> {
    val app = context.applicationContext
    if (!ConnectivityState.isOnline(app)) {
        return NetworkResult.Failure(NetworkError.Offline)
    }
    return try {
        val response = call()
        if (response.isSuccessful) {
            val body = response.body()
            if (body != null) {
                NetworkResult.Success(body)
            } else {
                @Suppress("UNCHECKED_CAST")
                NetworkResult.Success(Unit as T)
            }
        } else {
            NetworkResult.Failure(NetworkErrorMapper.fromResponse(response))
        }
    } catch (t: Throwable) {
        NetworkResult.Failure(NetworkErrorMapper.fromThrowable(t, ConnectivityState.isOnline(app)))
    }
}

suspend fun <T> safeApiCallRaw(
    context: Context,
    call: suspend () -> Response<T>
): NetworkResult<Response<T>> {
    val app = context.applicationContext
    if (!ConnectivityState.isOnline(app)) {
        return NetworkResult.Failure(NetworkError.Offline)
    }
    return try {
        NetworkResult.Success(call())
    } catch (t: Throwable) {
        NetworkResult.Failure(NetworkErrorMapper.fromThrowable(t, ConnectivityState.isOnline(app)))
    }
}
