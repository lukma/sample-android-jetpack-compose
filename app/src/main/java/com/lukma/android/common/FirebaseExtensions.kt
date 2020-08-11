package com.lukma.android.common

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.sendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.tasks.await

suspend fun FirebaseStorage.uploadFile(source: Uri, destination: String): Uri {
    val storageRef = reference
    val mediaRef = storageRef.child("$destination/${source.lastPathSegment}")
    val uploadTask = mediaRef.putFile(source).await().task
    return if (uploadTask.isSuccessful) {
        mediaRef.downloadUrl.await()
    } else {
        throw IllegalStateException("Can't find file url")
    }
}

@ExperimentalCoroutinesApi
suspend fun FirebaseStorage.uploadFileWithProgress(
    source: Uri,
    destination: String,
): Flow<Pair<Uri?, Double>> = channelFlow {
    val storageRef = reference
    val mediaRef = storageRef.child("$destination/${source.lastPathSegment}")
    val uploadTask = mediaRef.putFile(source)

    uploadTask
        .addOnProgressListener { task ->
            val progress = (100.0 * task.bytesTransferred) / task.totalByteCount
            sendBlocking(Pair(null, progress))
        }
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                CoroutineScope(Dispatchers.IO).launch {
                    val uri = mediaRef.downloadUrl.await()
                    sendBlocking(Pair(uri, 100.0))
                }
            } else {
                val error = IllegalStateException("Can't find file url")
                close(error)
            }
        }

    awaitClose { }
}
