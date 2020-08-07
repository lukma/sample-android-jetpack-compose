package com.lukma.android.data.auth

import com.google.firebase.auth.FirebaseAuth
import com.lukma.android.domain.auth.AuthRepository
import kotlinx.coroutines.tasks.await

class AuthRepositoryImpl(private val firebaseAuth: FirebaseAuth) : AuthRepository {
    override suspend fun isLoggedIn(): Boolean = firebaseAuth.currentUser != null

    override suspend fun signIn(email: String, password: String) {
        runCatching {
            firebaseAuth.signInWithEmailAndPassword(email, password).await()
        }.getOrThrow()
    }
}
