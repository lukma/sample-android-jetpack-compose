package com.lukma.android.ui

import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Providers
import androidx.compose.ui.platform.setContent
import com.lukma.android.common.ActivityResultHandler
import com.lukma.android.common.ActivityResultHandlerAmbient
import com.lukma.android.common.PermissionUtils
import com.lukma.android.common.PermissionUtilsAmbient
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val navigationViewModel by viewModels<NavigationViewModel>()
    private val permissionUtils by lazy { PermissionUtils(this) }
    private val activityResultHandler by lazy {
        ActivityResultHandler { callback ->
            registerForActivityResult(ActivityResultContracts.StartActivityForResult(), callback)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val navigationHandler = NavigationHandler(
            currentScreen = { navigationViewModel.currentScreen },
            onNavigateTo = navigationViewModel::navigateTo,
            onCheckIsLoggedIn = navigationViewModel::checkIsLoggedIn
        )
        setContent {
            Providers(
                NavigationHandlerAmbient provides navigationHandler,
                PermissionUtilsAmbient provides permissionUtils,
                ActivityResultHandlerAmbient provides activityResultHandler
            ) {
                MainContent()
            }
        }
    }

    override fun onBackPressed() {
        if (!navigationViewModel.onBack()) {
            super.onBackPressed()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionUtils.handlePermissionResult(requestCode, permissions, grantResults)
    }
}
