package com.lukma.android.domain

sealed class Either<out E, out V> {
    data class Error<out E>(val error: E) : Either<E, Nothing>()
    data class Value<out V>(val value: V) : Either<Nothing, V>()
}

suspend fun <V> either(block: suspend () -> V) = runCatching { Either.Value(block()) }
    .getOrElse { Either.Error(it) }

fun <V> Either<Throwable, V>.getOrNull() = when (this) {
    is Either.Value -> value
    is Either.Error -> null
}

inline val <T> Either<Throwable, T>.isSuccess: Boolean
    get() = this is Either.Value
