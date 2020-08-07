package com.lukma.android.domain.auth

interface AuthRepository {
    suspend fun isLoggedIn(): Boolean
    suspend fun signIn(email: String, password: String)
}
