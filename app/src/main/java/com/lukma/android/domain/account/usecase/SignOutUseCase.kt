package com.lukma.android.domain.account.usecase

import com.lukma.android.domain.BaseUseCase
import com.lukma.android.domain.account.AccountRepository
import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.CoroutineContext

class SignOutUseCase(
    private val accountRepository: AccountRepository
) : BaseUseCase<Unit>() {

    override val coroutineContext: CoroutineContext = Dispatchers.IO

    override suspend fun build() = accountRepository.signOut()
}
