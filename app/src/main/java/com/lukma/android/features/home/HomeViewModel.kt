package com.lukma.android.features.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.lukma.android.common.UiState
import com.lukma.android.domain.asUiState
import com.lukma.android.domain.post.Post
import com.lukma.android.domain.post.usecase.GetLatestPostsUseCase
import org.koin.core.KoinComponent
import org.koin.core.inject

class HomeViewModel : ViewModel(), KoinComponent {
    private val getLatestPostsUseCase by inject<GetLatestPostsUseCase>()

    private val postsMutable = MutableLiveData<UiState<List<Post>>>()
    internal val posts: LiveData<UiState<List<Post>>> = postsMutable

    suspend fun fetchLatestPosts() {
        postsMutable.value = UiState.Loading
        val result = getLatestPostsUseCase.invoke()
        postsMutable.postValue(result.asUiState)
    }
}
