package com.lukma.android.ui

import android.os.Bundle
import androidx.annotation.MainThread
import androidx.compose.runtime.*
import androidx.core.os.bundleOf
import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lukma.android.domain.auth.usecase.IsLoggedInUseCase
import com.lukma.android.domain.getOrNull
import kotlinx.coroutines.launch

enum class ScreenName { LOGIN, HOME, EXPLORE, PROFILE, CAPTURE }

sealed class Screen(val id: ScreenName) {
    object Login : Screen(ScreenName.HOME)
    object Home : Screen(ScreenName.HOME)
    object Explore : Screen(ScreenName.EXPLORE)
    object Profile : Screen(ScreenName.PROFILE)
    object Capture : Screen(ScreenName.CAPTURE)
}

private const val SCREEN_HISTORY = "screen_history"
private const val SCREEN_NAME = "screen_name"

private fun Screen.toBundle() = bundleOf(SCREEN_NAME to id.name)

private fun Bundle.toScreen() = when (ScreenName.valueOf(getStringOrThrow(SCREEN_NAME))) {
    ScreenName.LOGIN -> Screen.Login
    ScreenName.HOME -> Screen.Home
    ScreenName.EXPLORE -> Screen.Explore
    ScreenName.PROFILE -> Screen.Profile
    ScreenName.CAPTURE -> Screen.Capture
}

private fun Bundle.getStringOrThrow(key: String) =
    requireNotNull(getString(key)) { "Missing key '$key' in $this" }

fun <T> SavedStateHandle.getMutableStateOf(
    key: String,
    default: T,
    save: (T) -> Bundle,
    restore: (Bundle) -> T
): MutableState<T> {
    val bundle: Bundle? = get(key)
    val initial = bundle?.let(restore) ?: default
    val state = mutableStateOf(initial)
    setSavedStateProvider(key) {
        save(state.value)
    }
    return state
}

class NavigationViewModel @ViewModelInject constructor(
    private val isLoggedInUseCase: IsLoggedInUseCase,
    @Assisted savedStateHandle: SavedStateHandle
) : ViewModel() {

    private var isLoggedIn = false
        set(value) {
            currentScreen = if (value) Screen.Home else Screen.Login
            field = value
        }

    var currentScreen: Screen by savedStateHandle.getMutableStateOf<Screen>(
        key = SCREEN_HISTORY,
        default = if (isLoggedIn) Screen.Home else Screen.Login,
        save = { it.toBundle() },
        restore = { it.toScreen() }
    )
        private set

    fun checkIsLoggedIn() {
        viewModelScope.launch {
            val result = isLoggedInUseCase.invoke()
            isLoggedIn = result.getOrNull() == true
        }
    }

    fun navigateTo(screen: Screen) {
        currentScreen = screen
    }

    @MainThread
    fun onBack(): Boolean {
        val defaultScreen = if (isLoggedIn) Screen.Home else Screen.Login
        val wasHandled = currentScreen != defaultScreen
        currentScreen = defaultScreen
        return wasHandled
    }
}

class NavigationHandler(
    var currentScreen: () -> Screen,
    private val onNavigateTo: (Screen) -> Unit,
    private val onCheckIsLoggedIn: () -> Unit
) {

    fun checkIsLoggedIn() {
        onCheckIsLoggedIn.invoke()
    }

    fun navigateTo(screen: Screen) {
        onNavigateTo(screen)
    }
}

val NavigationHandlerAmbient = ambientOf<NavigationHandler> { error("Navigation not initialized") }
