package com.lukma.android.common

import androidx.compose.runtime.Composable

sealed class UiState<out T> {
    object None : UiState<Nothing>()
    object Loading : UiState<Nothing>()
    data class Success<out T>(val data: T) : UiState<T>()
    data class Failure(val error: Throwable) : UiState<Nothing>()
}

@Composable
fun <T> UiState<T>.onFailure(block: @Composable (Throwable) -> Unit) {
    if (this is UiState.Failure) {
        block(error)
    }
}
