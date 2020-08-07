package com.lukma.android.features.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lukma.android.common.UiState
import com.lukma.android.domain.asUiState
import com.lukma.android.domain.auth.usecase.SignInUseCase
import kotlinx.coroutines.launch
import org.koin.core.KoinComponent
import org.koin.core.inject

class LoginViewModel : ViewModel(), KoinComponent {
    private val signInUseCase by inject<SignInUseCase>()

    private val authResultMutable = MutableLiveData<UiState<Unit>>()
    internal val authResult: LiveData<UiState<Unit>> = authResultMutable

    fun signIn(username: String, password: String) {
        authResultMutable.value = UiState.Loading
        viewModelScope.launch {
            val result = signInUseCase.addParams(username, password).invoke()
            authResultMutable.postValue(result.asUiState)
        }
    }

    fun clearState() {
        authResultMutable.postValue(UiState.None)
    }
}
