package com.anchorguard.client.network

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import retrofit2.Response
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

object NetworkErrorMapper {
    private val gson = Gson()

    fun fromThrowable(t: Throwable, isOnline: Boolean = true): NetworkError {
        if (!isOnline) return NetworkError.Offline
        return when (t) {
            is SocketTimeoutException -> NetworkError.Timeout
            is UnknownHostException -> NetworkError.Offline
            is IOException -> if (isOfflineLike(t)) NetworkError.Offline else NetworkError.Unknown(t.message, t)
            else -> NetworkError.Unknown(t.message, t)
        }
    }

    fun <T> fromResponse(response: Response<T>): NetworkError {
        val code = response.code()
        val message = parseMessage(response.errorBody()?.string())
        return when (code) {
            401 -> NetworkError.Auth(code, message)
            403 -> {
                if (message != null && message.contains("tier", ignoreCase = true)) {
                    NetworkError.TierRequired(message)
                } else {
                    NetworkError.Auth(code, message)
                }
            }
            in 500..599 -> NetworkError.Server(code, message)
            else -> NetworkError.Server(code, message)
        }
    }

    fun parseMessage(raw: String?): String? {
        if (raw.isNullOrBlank()) return null
        return try {
            val obj = gson.fromJson(raw, Map::class.java)
            (obj["message"] ?: obj["error"] ?: obj["detail"])?.toString()
        } catch (_: JsonSyntaxException) {
            raw.take(160)
        } catch (_: Throwable) {
            raw.take(160)
        }
    }

    private fun isOfflineLike(t: IOException): Boolean {
        val msg = (t.message ?: "").lowercase()
        return msg.contains("unable to resolve host") ||
            msg.contains("failed to connect") ||
            msg.contains("network is unreachable") ||
            msg.contains("no address associated") ||
            msg.contains("software caused connection abort")
    }
}
