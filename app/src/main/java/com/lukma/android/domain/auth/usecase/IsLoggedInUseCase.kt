package com.lukma.android.domain.auth.usecase

import com.lukma.android.domain.BaseUseCase
import com.lukma.android.domain.auth.AuthRepository
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

class IsLoggedInUseCase @Inject constructor(
    private val authRepository: AuthRepository
) : BaseUseCase<Boolean>() {

    override val coroutineContext: CoroutineContext = Dispatchers.IO

    override suspend fun build(): Boolean = authRepository.isLoggedIn()
}
