package com.lukma.android.domain.account

interface AccountRepository {
    suspend fun getMyProfile(): Profile
    suspend fun updateMyProfile(displayName: String?, photo: String?)
    suspend fun signOut()
}
