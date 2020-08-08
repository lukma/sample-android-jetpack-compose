package com.lukma.android.domain.auth.usecase

import com.lukma.android.domain.BaseUseCase
import com.lukma.android.domain.auth.AuthRepository
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

class SignInUseCase @Inject constructor(
    private val authRepository: AuthRepository
) : BaseUseCase<Unit>() {

    fun addParams(email: String, password: String) = apply {
        val params = mapOf(
            KEY_EMAIL to email,
            KEY_PASSWORD to password
        )
        super.addParams(params)
    }

    override val coroutineContext: CoroutineContext = Dispatchers.IO

    override suspend fun build() {
        val email = params[KEY_EMAIL] as String
        val password = params[KEY_PASSWORD] as String
        return authRepository.signIn(email, password)
    }

    companion object {
        private const val KEY_EMAIL = "KEY_EMAIL"
        private const val KEY_PASSWORD = "KEY_PASSWORD"
    }
}
