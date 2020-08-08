package com.lukma.android.domain.post.usecase

import com.lukma.android.domain.BaseUseCase
import com.lukma.android.domain.post.Post
import com.lukma.android.domain.post.PostRepository
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

class GetRecommendedPostsUseCase @Inject constructor(
    private val postRepository: PostRepository
) : BaseUseCase<List<Post>>() {

    override val coroutineContext: CoroutineContext = Dispatchers.IO

    override suspend fun build(): List<Post> = postRepository.getRecommendedPosts()
}
