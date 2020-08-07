package com.lukma.android.ui

import android.Manifest
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Icon
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.launchInComposition
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.vectorResource
import androidx.ui.tooling.preview.Preview
import com.lukma.android.R
import com.lukma.android.common.PermissionUtilsAmbient
import com.lukma.android.features.capture.CaptureScreen
import com.lukma.android.features.explore.ExploreScreen
import com.lukma.android.features.home.HomeScreen
import com.lukma.android.features.login.LoginScreen
import com.lukma.android.features.profile.ProfileScreen
import com.lukma.android.ui.theme.CleanTheme

@Composable
fun MainContent() {
    val navigation = NavigationHandlerAmbient.current
    launchInComposition {
        navigation.checkIsLoggedIn()
    }

    CleanTheme {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colors.background
        ) {
            when (navigation.currentScreen()) {
                is Screen.Login -> LoginScreen()
                is Screen.Capture -> CaptureScreen()
                else -> AppScaffold()
            }
        }
    }
}

@Composable
fun AppScaffold() {
    val scaffoldState = rememberScaffoldState()
    Scaffold(
        scaffoldState = scaffoldState,
        bottomBar = { AppBottomBar() },
        floatingActionButton = { AppFloatingActionButton() },
        isFloatingActionButtonDocked = true,
        bodyContent = { AppBody(Modifier.padding(bottom = it.bottom)) }
    )
}

@Composable
fun AppFloatingActionButton() {
    val permissionUtils = PermissionUtilsAmbient.current
    val navigation = NavigationHandlerAmbient.current
    FloatingActionButton(onClick = {
        permissionUtils.runWithPermission(arrayOf(Manifest.permission.CAMERA)) {
            navigation.navigateTo(Screen.Capture)
        }
    }) {
        Icon(asset = vectorResource(id = R.drawable.ic_twotone_camera_24))
    }
}

@Composable
fun AppBottomBar() {
    val navigation = NavigationHandlerAmbient.current
    BottomAppBar {
        IconButton(onClick = { navigation.navigateTo(Screen.Home) }) {
            Icon(Icons.Filled.Home, tint = tintColor(navigation.currentScreen() is Screen.Home))
        }
        IconButton(onClick = { navigation.navigateTo(Screen.Explore) }) {
            Icon(
                Icons.Filled.Search,
                tint = tintColor(navigation.currentScreen() is Screen.Explore)
            )
        }
        IconButton(onClick = { navigation.navigateTo(Screen.Profile) }) {
            Icon(Icons.Filled.Face, tint = tintColor(navigation.currentScreen() is Screen.Profile))
        }
    }
}

@Composable
private fun tintColor(isSelected: Boolean) = if (isSelected) Color.White else Color.LightGray

@Composable
fun AppBody(modifier: Modifier) {
    val navigation = NavigationHandlerAmbient.current
    Crossfade(current = navigation.currentScreen(), modifier = modifier) {
        when (it) {
            is Screen.Home -> HomeScreen()
            is Screen.Explore -> ExploreScreen()
            is Screen.Profile -> ProfileScreen()
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    CleanTheme {
        AppScaffold()
    }
}
