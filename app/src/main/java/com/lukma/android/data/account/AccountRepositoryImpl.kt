package com.lukma.android.data.account

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.storage.FirebaseStorage
import com.lukma.android.common.uploadFile
import com.lukma.android.domain.account.AccountRepository
import com.lukma.android.domain.account.Profile
import kotlinx.coroutines.tasks.await
import java.io.File

class AccountRepositoryImpl(
    private val firebaseAuth: FirebaseAuth,
    private val firebaseStorage: FirebaseStorage
) : AccountRepository {

    override suspend fun getMyProfile(): Profile {
        val uid = firebaseAuth.currentUser?.uid ?: throw NullPointerException()
        val email = firebaseAuth.currentUser?.email ?: throw NullPointerException()
        val displayName = firebaseAuth.currentUser?.displayName ?: ""
        val photoUrl = firebaseAuth.currentUser?.photoUrl?.toString() ?: ""
        return Profile(uid, email, displayName, photoUrl)
    }

    override suspend fun updateMyProfile(displayName: String?, photo: String?) {
        val myProfile = getMyProfile()

        val request = UserProfileChangeRequest.Builder()
            .apply {
                displayName?.run { setDisplayName(this) }
                photo
                    ?.let {
                        val destination = "accounts/${myProfile.uid}/photos"
                        firebaseStorage.uploadFile(Uri.fromFile(File(it)), destination)
                    }
                    ?.run { photoUri = this }
            }
            .build()
        firebaseAuth.currentUser?.updateProfile(request)?.await()
    }

    override suspend fun signOut() {
        firebaseAuth.signOut()
    }
}
