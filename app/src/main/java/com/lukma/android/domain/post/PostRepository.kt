package com.lukma.android.domain.post

interface PostRepository {
    suspend fun getLatestPosts(): List<Post>
    suspend fun getRecommendedPosts(): List<Post>
    suspend fun createPost(post: Post)
    suspend fun updatePostAuthor(author: Post.Author)
}
