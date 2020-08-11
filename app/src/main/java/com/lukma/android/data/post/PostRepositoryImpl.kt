package com.lukma.android.data.post

import android.net.Uri
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.lukma.android.common.uploadFileWithProgress
import com.lukma.android.domain.post.Post
import com.lukma.android.domain.post.PostRepository
import com.lukma.android.domain.post.PostUploadInfo
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.sendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collectLatest
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

    @ExperimentalCoroutinesApi
    override suspend fun createPost(post: Post): Flow<PostUploadInfo> = channelFlow {
        val author = post.author.let(::transform)

        val destination = "posts/${post.author.uid}"
        when (post) {
            is Post.Image -> {
                firebaseStorage.uploadFileWithProgress(Uri.fromFile(File(post.url)), destination)
                    .collectLatest {
                        if (it.first != null) {
                            val postToAdd = PostImageData(
                                type = "image",
                                url = it.first.toString(),
                                author = author
                            )
                            postsCollection.add(postToAdd).await()
                        }
                        sendBlocking(PostUploadInfo(it.second))
                    }
            }
            is Post.Video -> {
                var videoUploadInfo: Pair<Uri?, Double> = Pair(null, 0.0)
                var thumbnailUploadInfo: Pair<Uri?, Double> = Pair(null, 0.0)

                firebaseStorage.uploadFileWithProgress(Uri.fromFile(File(post.url)), destination)
                    .collectLatest {
                        videoUploadInfo = it
                        val progress = (videoUploadInfo.second + thumbnailUploadInfo.second) / 2
                        sendBlocking(PostUploadInfo(progress))
                    }

                firebaseStorage.uploadFileWithProgress(
                    Uri.fromFile(File(post.thumbnail)),
                    destination
                )
                    .collectLatest {
                        thumbnailUploadInfo = it
                        val progress = (videoUploadInfo.second + thumbnailUploadInfo.second) / 2
                        sendBlocking(PostUploadInfo(progress))
                    }

                while (videoUploadInfo.first != null && thumbnailUploadInfo.first != null) {
                    val postToAdd = PostVideoData(
                        type = "video",
                        url = videoUploadInfo.first.toString(),
                        thumbnail = thumbnailUploadInfo.first.toString(),
                        author = author
                    )
                    postsCollection.add(postToAdd).await()
                }
            }
        }

        awaitClose { }
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
