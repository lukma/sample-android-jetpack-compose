package com.lukma.android.data.post

import android.net.Uri
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.lukma.android.common.uploadFile
import com.lukma.android.domain.post.Post
import com.lukma.android.domain.post.PostRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.tasks.await
import java.io.File

class PostRepositoryImpl(
    private val firebaseStorage: FirebaseStorage,
    private val postsCollection: CollectionReference
) : PostRepository {

    override suspend fun getLatestPosts(): List<Post> = postsCollection
        .orderBy("createdAt", Query.Direction.DESCENDING)
        .get()
        .await()
        .documents
        .mapNotNull { transform(it) }

    override suspend fun getRecommendedPosts(): List<Post> = postsCollection
        .orderBy("likeCount", Query.Direction.DESCENDING)
        .get()
        .await()
        .documents
        .mapNotNull { transform(it) }

    override suspend fun createPost(post: Post) {
        val author = post.author.let(::transform)

        val destination = "posts/${post.author.uid}"
        val postToAdd: Any = when (post) {
            is Post.Image -> {
                val url =
                    firebaseStorage.uploadFile(Uri.fromFile(File(post.url)), destination).toString()
                PostImageData(
                    type = "image",
                    url = url,
                    author = author
                )
            }
            is Post.Video -> {
                val url =
                    firebaseStorage.uploadFile(Uri.fromFile(File(post.url)), destination).toString()
                val thumbnail =
                    firebaseStorage.uploadFile(Uri.fromFile(File(post.thumbnail)), destination)
                        .toString()
                PostVideoData(
                    type = "video",
                    url = url,
                    thumbnail = thumbnail,
                    author = author
                )
            }
        }
        postsCollection.add(postToAdd).await()
    }

    override suspend fun updatePostAuthor(author: Post.Author) {
        val authorData = AuthorData(
            uid = author.uid,
            name = author.name,
            photo = author.photo
        )

        val scope = CoroutineScope(Dispatchers.IO)
        val tasks = postsCollection.whereEqualTo("author.uid", author.uid)
            .get()
            .await()
            .documents
            .map {
                scope.async {
                    it.reference.update(mapOf("author" to authorData))
                }
            }
        tasks.awaitAll()
    }
}
