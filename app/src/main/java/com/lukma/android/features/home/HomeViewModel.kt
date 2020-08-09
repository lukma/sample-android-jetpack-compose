package com.lukma.android.features.home

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.lukma.android.common.UiState
import com.lukma.android.common.asUiState
import com.lukma.android.domain.post.Post
import com.lukma.android.domain.post.usecase.GetLatestPostsUseCase

class HomeViewModel @ViewModelInject constructor(
    private val getLatestPostsUseCase: GetLatestPostsUseCase
) : ViewModel() {

    private val postsMutable = MutableLiveData<UiState<List<Post>>>()
    internal val posts: LiveData<UiState<List<Post>>> = postsMutable

    suspend fun fetchLatestPosts() {
        postsMutable.value = UiState.Loading
        val result = getLatestPostsUseCase.invoke()
        postsMutable.postValue(result.asUiState)
    }
}
