package com.lukma.android.common

import androidx.compose.runtime.Composable
import com.lukma.android.domain.Either

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

inline val <T> Either<Throwable, T>.asUiState: UiState<T>
    get() = when (this) {
        is Either.Value -> UiState.Success(value)
        is Either.Error -> UiState.Failure(error)
    }
