package com.lukma.android.domain.account

data class Profile(
    val uid: String,
    val email: String,
    val displayName: String,
    val photo: String
)
