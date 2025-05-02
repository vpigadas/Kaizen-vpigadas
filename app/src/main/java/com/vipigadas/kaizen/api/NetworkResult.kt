package com.vipigadas.kaizen.api

/**
 * A sealed class that represents all possible states of a network request
 * This is a more robust alternative to Result<T> for API responses
 */
sealed class NetworkResult<out T> {
    /**
     * Represents a successful API call with data
     */
    data class Success<T>(val data: T) : NetworkResult<T>()

    /**
     * Represents an error response from the API with code and message
     */
    data class Error(val code: Int, val message: String) : NetworkResult<Nothing>()

    /**
     * Represents an exception that occurred during the API call
     */
    data class Exception(val exception: Throwable) : NetworkResult<Nothing>()

    /**
     * Represents a loading state for the API call
     */
    class Loading<T> : NetworkResult<T>()

    /**
     * Helper method to transform the data of a Success result
     */
    inline fun <R> map(transform: (T) -> R): NetworkResult<R> {
        return when (this) {
            is Success -> Success(transform(data))
            is Error -> this
            is Exception -> this
            is Loading -> Loading()
        }
    }

    /**
     * Get data or null if not Success
     */
    fun getOrNull(): T? {
        return when (this) {
            is Success -> data
            else -> null
        }
    }

    /**
     * Check if the result is a success
     */
    fun isSuccess(): Boolean = this is Success
}