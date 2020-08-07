package com.lukma.android.common

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
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
