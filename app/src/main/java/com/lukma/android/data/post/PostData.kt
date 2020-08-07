package com.lukma.android.data.post

import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp
import com.lukma.android.domain.post.Post

data class AuthorData(
    val uid: String? = null,
    val name: String? = null,
    val photo: String? = null
) {
    fun toEntity() = runCatching {
        Post.Author(
            uid = uid!!,
            name = name!!,
            photo = photo!!
        )
    }.getOrNull()
}

data class PostImageData(
    val type: String? = null,
    val url: String? = null,
    val author: AuthorData? = null,
    val likeCount: Int = 0,
    @ServerTimestamp
    var createdAt: Timestamp? = null
) {
    fun toEntity() = runCatching {
        Post.Image(
            url!!,
            author?.toEntity()!!,
            likeCount
        )
    }.getOrNull()
}

data class PostVideoData(
    val type: String? = null,
    val url: String? = null,
    val thumbnail: String? = null,
    val author: AuthorData? = null,
    val likeCount: Int = 0,
    @ServerTimestamp
    var createdAt: Timestamp? = null
) {
    fun toEntity() = runCatching {
        Post.Video(
            url!!,
            thumbnail!!,
            author?.toEntity()!!,
            likeCount
        )
    }.getOrNull()
}
