package com.lukma.android.domain.account.usecase

import com.lukma.android.domain.BaseUseCase
import com.lukma.android.domain.account.AccountRepository
import com.lukma.android.domain.account.Profile
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

class GetMyProfileUseCase @Inject constructor(
    private val accountRepository: AccountRepository
) : BaseUseCase<Profile>() {

    override val coroutineContext: CoroutineContext = Dispatchers.IO

    override suspend fun build(): Profile = accountRepository.getMyProfile()
}
