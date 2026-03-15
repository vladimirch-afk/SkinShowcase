package ru.kotlix.skinshowcase.core

/**
 * Wrapper for async operation result.
 * Use from repository/use-case layer; avoid exposing raw exceptions to UI.
 */
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val throwable: Throwable) : Result<Nothing>()
    data object Loading : Result<Nothing>()
}
