package com.lukma.android.domain

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.withContext

abstract class BaseUseCase<out T> : CoroutineScope {
    protected lateinit var params: Map<String, Any?>

    protected fun addParams(params: Map<String, Any?>) = apply { this.params = params }

    protected abstract suspend fun build(): T

    suspend fun invoke() = withContext(coroutineContext) {
        either(::build)
    }
}
