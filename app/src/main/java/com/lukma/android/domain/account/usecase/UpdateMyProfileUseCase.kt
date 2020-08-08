package com.lukma.android.domain.account.usecase

import com.lukma.android.domain.BaseUseCase
import com.lukma.android.domain.account.AccountRepository
import com.lukma.android.domain.post.Post
import com.lukma.android.domain.post.PostRepository
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

class UpdateMyProfileUseCase @Inject constructor(
    private val accountRepository: AccountRepository,
    private val postRepository: PostRepository
) : BaseUseCase<Unit>() {

    fun addParams(displayName: String?, photo: String?) = apply {
        val params = mapOf(
            KEY_DISPLAY_NAME to displayName,
            KEY_PHOTO to photo
        )
        super.addParams(params)
    }

    override val coroutineContext: CoroutineContext = Dispatchers.IO

    override suspend fun build() {
        val displayName = params[KEY_DISPLAY_NAME] as String?
        val photo = params[KEY_PHOTO] as String?
        accountRepository.updateMyProfile(displayName, photo)

        val myProfile = accountRepository.getMyProfile()
        val author = Post.Author(
            uid = myProfile.uid,
            name = myProfile.displayName,
            photo = myProfile.photo
        )
        postRepository.updatePostAuthor(author)
    }

    companion object {
        private const val KEY_DISPLAY_NAME = "KEY_DISPLAY_NAME"
        private const val KEY_PHOTO = "KEY_PHOTO"
    }
}
