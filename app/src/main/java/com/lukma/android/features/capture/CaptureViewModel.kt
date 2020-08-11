package com.lukma.android.features.capture

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.lukma.android.common.UiState

class CaptureViewModel : ViewModel() {
    private val createPostResultMutable = MutableLiveData<UiState<String>>()
    internal val createPostResult: LiveData<UiState<String>> = createPostResultMutable

    fun prepareCreatePost(filePath: String) {
        createPostResultMutable.postValue(UiState.Success(filePath))
    }

    fun takePictureFailed() {
        val value = UiState.Failure(Exception("Take picture failed"))
        createPostResultMutable.postValue(value)
    }

    fun prevUploadIncomplete() {
        val value = UiState.Failure(Exception("Previous create post task not yet completed"))
        createPostResultMutable.postValue(value)
    }

    fun clearState() {
        createPostResultMutable.postValue(UiState.None)
    }
}
