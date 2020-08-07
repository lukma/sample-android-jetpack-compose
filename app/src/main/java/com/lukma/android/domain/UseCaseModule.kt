package com.lukma.android.domain

import com.lukma.android.domain.account.usecase.GetMyProfileUseCase
import com.lukma.android.domain.account.usecase.SignOutUseCase
import com.lukma.android.domain.account.usecase.UpdateMyProfileUseCase
import com.lukma.android.domain.auth.usecase.IsLoggedInUseCase
import com.lukma.android.domain.auth.usecase.SignInUseCase
import com.lukma.android.domain.post.usecase.CreatePostUseCase
import com.lukma.android.domain.post.usecase.GetLatestPostsUseCase
import com.lukma.android.domain.post.usecase.GetRecommendedPostsUseCase
import org.koin.dsl.module
import org.koin.experimental.builder.factory

val useCaseModule = module {
    // Auth
    factory<IsLoggedInUseCase>()
    factory<SignInUseCase>()

    // Account
    factory<GetMyProfileUseCase>()
    factory<UpdateMyProfileUseCase>()
    factory<SignOutUseCase>()

    // Post
    factory<GetLatestPostsUseCase>()
    factory<GetRecommendedPostsUseCase>()
    factory<CreatePostUseCase>()
}
