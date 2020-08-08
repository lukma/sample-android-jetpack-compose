package com.lukma.android.features.explore

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.lukma.android.common.UiState
import com.lukma.android.domain.asUiState
import com.lukma.android.domain.post.Post
import com.lukma.android.domain.post.usecase.GetRecommendedPostsUseCase

class ExploreViewModel @ViewModelInject constructor(
    private val getRecommendedPostsUseCase: GetRecommendedPostsUseCase
) : ViewModel() {

    private val postsMutable = MutableLiveData<UiState<List<Post>>>()
    internal val posts: LiveData<UiState<List<Post>>> = postsMutable

    suspend fun fetchRecommendPosts() {
        postsMutable.value = UiState.Loading
        val result = getRecommendedPostsUseCase.invoke()
        postsMutable.postValue(result.asUiState)
    }
}
