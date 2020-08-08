package com.lukma.android.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.firestoreSettings
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import com.lukma.android.data.account.AccountRepositoryImpl
import com.lukma.android.data.auth.AuthRepositoryImpl
import com.lukma.android.data.post.PostRepositoryImpl
import com.lukma.android.domain.account.AccountRepository
import com.lukma.android.domain.auth.AuthRepository
import com.lukma.android.domain.post.PostRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import javax.inject.Singleton

@Module
@InstallIn(ApplicationComponent::class)
class DataModule {

    @Singleton
    @Provides
    fun bindFirebaseAuth() = Firebase.auth

    @Singleton
    @Provides
    fun bindFirebaseStorage() = Firebase.storage

    @Singleton
    @Provides
    fun bindFirebaseFirestore() = Firebase.firestore.apply {
        firestoreSettings = firestoreSettings {
            isPersistenceEnabled = true
        }
    }

    @Provides
    fun bindAuthRepository(firebaseAuth: FirebaseAuth): AuthRepository =
        AuthRepositoryImpl(firebaseAuth)

    @Provides
    fun bindAccountRepository(
        firebaseAuth: FirebaseAuth,
        firebaseStorage: FirebaseStorage
    ): AccountRepository = AccountRepositoryImpl(firebaseAuth, firebaseStorage)

    @Provides
    fun bindPostRepository(
        firebaseStorage: FirebaseStorage,
        firebaseFirestore: FirebaseFirestore
    ): PostRepository = PostRepositoryImpl(firebaseStorage, firebaseFirestore.collection("posts"))
}
