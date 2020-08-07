package com.lukma.android.domain.auth.usecase

import com.lukma.android.domain.BaseUseCase
import com.lukma.android.domain.auth.AuthRepository
import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.CoroutineContext

class IsLoggedInUseCase(
    private val authRepository: AuthRepository
) : BaseUseCase<Boolean>() {

    override val coroutineContext: CoroutineContext = Dispatchers.IO

    override suspend fun build(): Boolean = authRepository.isLoggedIn()
}
