package com.anchorguard.client.network

sealed interface NetworkError {
    data object Offline : NetworkError
    data object Timeout : NetworkError
    data class Auth(val code: Int? = 401, val message: String? = null) : NetworkError
    data class Server(val code: Int, val message: String? = null) : NetworkError
    data class Unknown(val message: String? = null, val cause: Throwable? = null) : NetworkError

    fun userMessage(): String = when (this) {
        is Offline -> "You're offline. Changes will sync when reconnected."
        is Timeout -> "Request timed out. Please try again."
        is Auth -> message?.takeIf { it.isNotBlank() } ?: "Session expired. Please sign in again."
        is Server -> message?.takeIf { it.isNotBlank() } ?: "Server error ($code). Please try again."
        is Unknown -> message?.takeIf { it.isNotBlank() } ?: "Something went wrong. Please try again."
    }
}

sealed interface NetworkResult<out T> {
    data class Success<T>(val data: T) : NetworkResult<T>
    data class Failure(val error: NetworkError) : NetworkResult<Nothing>

    fun <R> map(transform: (T) -> R): NetworkResult<R> = when (this) {
        is Success -> Success(transform(data))
        is Failure -> this
    }

    fun getOrNull(): T? = when (this) {
        is Success -> data
        is Failure -> null
    }
}

inline fun <T, R> NetworkResult<T>.fold(onSuccess: (T) -> R, onFailure: (NetworkError) -> R): R =
    when (this) {
        is NetworkResult.Success -> onSuccess(data)
        is NetworkResult.Failure -> onFailure(error)
    }
