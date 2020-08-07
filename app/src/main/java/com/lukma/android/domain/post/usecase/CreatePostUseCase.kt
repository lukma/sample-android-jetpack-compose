package com.lukma.android.domain.post.usecase

import com.lukma.android.domain.BaseUseCase
import com.lukma.android.domain.account.AccountRepository
import com.lukma.android.domain.post.Post
import com.lukma.android.domain.post.PostRepository
import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.CoroutineContext

class CreatePostUseCase(
    private val postRepository: PostRepository,
    private val accountRepository: AccountRepository
) : BaseUseCase<Unit>() {

    fun addParams(url: String) = apply {
        val params = mapOf(
            KEY_CREATE_TYPE to CreateType.IMAGE,
            KEY_URL to url
        )
        super.addParams(params)
    }

    fun addParams(url: String, thumbnail: String) = apply {
        val params = mapOf(
            KEY_CREATE_TYPE to CreateType.VIDEO,
            KEY_URL to url,
            KEY_THUMBNAiL to thumbnail
        )
        super.addParams(params)
    }

    override val coroutineContext: CoroutineContext = Dispatchers.IO

    override suspend fun build() {
        val myProfile = accountRepository.getMyProfile()
        val author = Post.Author(
            uid = myProfile.uid,
            name = myProfile.displayName,
            photo = myProfile.photo
        )

        val newPost = when (params[KEY_CREATE_TYPE] as CreateType) {
            CreateType.IMAGE -> Post.Image(
                url = params[KEY_URL] as String,
                author = author
            )
            CreateType.VIDEO -> Post.Video(
                url = params[KEY_URL] as String,
                thumbnail = params[KEY_THUMBNAiL] as String,
                author = author
            )
        }

        return postRepository.createPost(newPost)
    }

    companion object {
        private const val KEY_CREATE_TYPE = "KEY_CREATE_TYPE"
        private const val KEY_URL = "KEY_URL"
        private const val KEY_THUMBNAiL = "KEY_THUMBNAiL"
    }

    private enum class CreateType { IMAGE, VIDEO }
}
