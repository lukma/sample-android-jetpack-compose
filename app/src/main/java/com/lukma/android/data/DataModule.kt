package com.lukma.android.data

import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.firestoreSettings
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.lukma.android.data.account.AccountRepositoryImpl
import com.lukma.android.data.auth.AuthRepositoryImpl
import com.lukma.android.data.post.PostRepositoryImpl
import com.lukma.android.domain.account.AccountRepository
import com.lukma.android.domain.auth.AuthRepository
import com.lukma.android.domain.post.PostRepository
import org.koin.dsl.module
import org.koin.experimental.builder.factoryBy

val dataModule = module {
    // Data Source
    single { Firebase.auth }
    single {
        Firebase.firestore.apply {
            firestoreSettings = firestoreSettings {
                isPersistenceEnabled = true
            }
        }
    }
    single { Firebase.storage }

    // Repository
    factoryBy<AuthRepository, AuthRepositoryImpl>()
    factory<AccountRepository> {
        AccountRepositoryImpl(
            firebaseAuth = get(),
            firebaseStorage = get()
        )
    }
    factory<PostRepository> {
        PostRepositoryImpl(
            firebaseStorage = get(),
            postsCollection = get<FirebaseFirestore>().collection("posts")
        )
    }
}
