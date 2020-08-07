package com.lukma.android.domain.post

sealed class Post(
    open val author: Author,
    open val likeCount: Int,
) {
    data class Image(
        val url: String,
        override val author: Author,
        override val likeCount: Int = 0
    ) : Post(author, likeCount)

    data class Video(
        val url: String,
        val thumbnail: String,
        override val author: Author,
        override val likeCount: Int = 0
    ) : Post(author, likeCount)

    data class Author(
        val uid: String,
        val name: String,
        val photo: String? = null
    )
}
