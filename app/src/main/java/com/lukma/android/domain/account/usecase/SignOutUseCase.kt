package com.lukma.android.domain.account.usecase

import com.lukma.android.domain.BaseUseCase
import com.lukma.android.domain.account.AccountRepository
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

class SignOutUseCase @Inject constructor(
    private val accountRepository: AccountRepository
) : BaseUseCase<Unit>() {

    override val coroutineContext: CoroutineContext = Dispatchers.IO

    override suspend fun build() = accountRepository.signOut()
}
