package com.lukma.android.data.post

import com.google.firebase.firestore.DocumentSnapshot
import com.lukma.android.domain.post.Post

fun transform(document: DocumentSnapshot) = if ((document.get("type") as? String) == "image") {
    document.toObject(PostImageData::class.java)?.toEntity()
} else {
    document.toObject(PostVideoData::class.java)?.toEntity()
}

fun transform(author: Post.Author): AuthorData {
    val uid = author.uid
    val name = author.name
    val photo = author.photo
    return AuthorData(uid, name, photo)
}
