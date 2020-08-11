package com.lukma.android.ui

import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Providers
import androidx.compose.ui.platform.setContent
import com.lukma.android.common.*
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
        val workerWatcher = WorkerWatcher(this)
        setContent {
            Providers(
                NavigationHandlerAmbient provides navigationHandler,
                PermissionUtilsAmbient provides permissionUtils,
                ActivityResultHandlerAmbient provides activityResultHandler,
                WorkerWatcherAmbient provides workerWatcher
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
