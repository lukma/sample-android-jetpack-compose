package com.lukma.android.domain.post.usecase

import com.lukma.android.domain.BaseUseCase
import com.lukma.android.domain.post.Post
import com.lukma.android.domain.post.PostRepository
import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.CoroutineContext

class GetLatestPostsUseCase(
    private val postRepository: PostRepository
) : BaseUseCase<List<Post>>() {

    override val coroutineContext: CoroutineContext = Dispatchers.IO

    override suspend fun build(): List<Post> = postRepository.getLatestPosts()
}
