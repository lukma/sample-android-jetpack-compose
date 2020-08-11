package com.lukma.android.domain.post

import kotlinx.coroutines.flow.Flow

interface PostRepository {
    suspend fun getLatestPosts(): List<Post>
    suspend fun getRecommendedPosts(): List<Post>
    suspend fun createPost(post: Post): Flow<PostUploadInfo>
    suspend fun updatePostAuthor(author: Post.Author)
}
