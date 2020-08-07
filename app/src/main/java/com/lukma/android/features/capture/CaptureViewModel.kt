package com.lukma.android.features.capture

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.lukma.android.common.UiState
import com.lukma.android.domain.asUiState
import com.lukma.android.domain.post.usecase.CreatePostUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.KoinComponent
import org.koin.core.inject

class CaptureViewModel : ViewModel(), KoinComponent {
    private val createPostUseCase by inject<CreatePostUseCase>()

    private val createPostResultMutable = MutableLiveData<UiState<Unit>>()
    internal val createPostResult: LiveData<UiState<Unit>> = createPostResultMutable

    fun createPost(mediaPath: String) {
        createPostResultMutable.postValue(UiState.Loading)
        CoroutineScope(Dispatchers.IO).launch {
            val result = createPostUseCase
                .addParams(url = mediaPath)
                .invoke()
            createPostResultMutable.postValue(result.asUiState)
        }
    }

    fun takePictureFailed() {
        val value = UiState.Failure(Exception("Take picture failed"))
        createPostResultMutable.postValue(value)
    }

    fun clearState() {
        createPostResultMutable.postValue(UiState.None)
    }
}
