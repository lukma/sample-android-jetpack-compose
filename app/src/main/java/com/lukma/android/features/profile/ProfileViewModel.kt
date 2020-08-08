package com.lukma.android.features.profile

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lukma.android.common.UiState
import com.lukma.android.domain.account.Profile
import com.lukma.android.domain.account.usecase.GetMyProfileUseCase
import com.lukma.android.domain.account.usecase.SignOutUseCase
import com.lukma.android.domain.account.usecase.UpdateMyProfileUseCase
import com.lukma.android.domain.asUiState
import com.lukma.android.domain.getOrNull
import com.lukma.android.domain.isSuccess
import kotlinx.coroutines.launch

class ProfileViewModel @ViewModelInject constructor(
    private val getMyProfileUseCase: GetMyProfileUseCase,
    private val updateMyProfileUseCase: UpdateMyProfileUseCase,
    private val signOutUseCase: SignOutUseCase
) : ViewModel() {

    private val myProfileMutable = MutableLiveData<Profile>()
    internal val myProfile: LiveData<Profile> = myProfileMutable

    private val signOutResultMutable = MutableLiveData<UiState<Unit>>()
    internal val signOutResult: LiveData<UiState<Unit>> = signOutResultMutable

    fun fetchMyProfile() {
        viewModelScope.launch {
            val result = getMyProfileUseCase.invoke().getOrNull()
            result?.run(myProfileMutable::postValue)
        }
    }

    fun updateMyProfile(displayName: String? = null, photo: String? = null) {
        if (displayName == null && photo == null) return

        viewModelScope.launch {
            val result = updateMyProfileUseCase.addParams(displayName, photo).invoke()
            if (result.isSuccess) {
                fetchMyProfile()
            }
        }
    }

    fun signOut() {
        signOutResultMutable.value = UiState.Loading
        viewModelScope.launch {
            val result = signOutUseCase.invoke()
            signOutResultMutable.postValue(result.asUiState)
        }
    }

    fun clearState() {
        signOutResultMutable.postValue(UiState.None)
    }
}
