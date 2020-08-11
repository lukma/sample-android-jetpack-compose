package com.lukma.android.features.capture

import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Box
import androidx.compose.foundation.ContentGravity
import androidx.compose.foundation.Icon
import androidx.compose.foundation.layout.ConstraintLayout
import androidx.compose.foundation.layout.Dimension
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.launchInComposition
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.onDispose
import androidx.compose.runtime.savedinstancestate.savedInstanceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ContextAmbient
import androidx.compose.ui.platform.LifecycleOwnerAmbient
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.viewinterop.viewModel
import androidx.ui.tooling.preview.Preview
import androidx.work.WorkInfo
import com.lukma.android.R
import com.lukma.android.common.*
import com.lukma.android.common.ui.DelayedSnackbar
import com.lukma.android.ui.NavigationHandlerAmbient
import com.lukma.android.ui.Screen
import com.lukma.android.ui.theme.CleanTheme
import com.lukma.android.worker.UploadPostWork

@Composable
fun CaptureScreen() {
    val context = ContextAmbient.current
    val lifecycleOwner = LifecycleOwnerAmbient.current
    val workerWatcher = WorkerWatcherAmbient.current

    val cameraHandler = CameraHandler(lifecycleOwner)

    var isRunning = false
    workerWatcher.watch(UploadPostWork.TAG)
    workerWatcher.workInfo?.let { workInfo ->
        val workInfoState by workInfo.observeAsState()
        isRunning = workInfoState?.state == WorkInfo.State.RUNNING
    }

    val viewModel = viewModel<CaptureViewModel>()
    val createPostState by viewModel.createPostResult.observeAsState(initial = UiState.None)

    createPostState.onSuccess { filePath ->
        UploadPostWork.start(context = context, filePath = filePath)

        val navigation = NavigationHandlerAmbient.current
        navigation.navigateTo(Screen.Home)
    }

    launchInComposition {
        cameraHandler.prepare()
    }

    ConstraintLayout(Modifier.fillMaxSize()) {
        val (captureButton, flipButton, flashButton, errorMessage) = createRefs()

        CameraPreview(cameraHandler = cameraHandler, modifier = Modifier.fillMaxSize())
        FloatingActionButton(
            onClick = {
                if (!isRunning) {
                    cameraHandler.takePicture(
                        context = context,
                        onSuccess = viewModel::prepareCreatePost,
                        onError = viewModel::takePictureFailed
                    )
                } else {
                    viewModel.prevUploadIncomplete()
                }
            },
            modifier = Modifier.constrainAs(captureButton) {
                bottom.linkTo(parent.bottom, margin = 16.dp)
                end.linkTo(parent.end)
                start.linkTo(parent.start)
            },
            backgroundColor = Color.White
        ) { }

        val isBackCamera = savedInstanceState { cameraHandler.isBackCamera }
        IconButton(onClick = {
            isBackCamera.value = if (!isBackCamera.value) {
                true
            } else {
                !cameraHandler.isSupportFrontCamera()
            }

            cameraHandler.isBackCamera = isBackCamera.value
        }, modifier = Modifier.constrainAs(flipButton) {
            bottom.linkTo(captureButton.bottom)
            end.linkTo(captureButton.start, margin = 8.dp)
            top.linkTo(captureButton.top)
        }) {
            val tint = if (cameraHandler.isSupportFrontCamera()) Color.White else Color.LightGray
            Icon(
                asset = vectorResource(id = R.drawable.ic_twotone_flip_camera_android_24),
                tint = tint
            )
        }

        val isFlashOff = savedInstanceState { true }
        IconButton(onClick = {
            isFlashOff.value = !isFlashOff.value
            cameraHandler.isTorchEnable = isFlashOff.value
        }, modifier = Modifier.constrainAs(flashButton) {
            bottom.linkTo(captureButton.bottom)
            start.linkTo(captureButton.end, margin = 8.dp)
            top.linkTo(captureButton.top)
        }) {
            val assetId = if (isFlashOff.value) {
                R.drawable.ic_twotone_flash_off_24
            } else {
                R.drawable.ic_twotone_flash_on_24
            }
            Icon(
                asset = vectorResource(id = assetId),
                tint = Color.White
            )
        }

        if (createPostState is UiState.Loading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                backgroundColor = Color(0x80CCCCCC),
                gravity = ContentGravity.Center
            ) {
                CircularProgressIndicator()
            }
        }

        createPostState.onFailure {
            val text = it.localizedMessage ?: stringResource(id = R.string.error_default)
            DelayedSnackbar(text = text, modifier = Modifier.constrainAs(errorMessage) {
                bottom.linkTo(parent.bottom)
                width = Dimension.fillToConstraints
            })
        }
    }

    onDispose {
        viewModel.clearState()
        cameraHandler.destroy()
    }
}

@Composable
fun CameraPreview(cameraHandler: CameraHandler, modifier: Modifier) {
    AndroidView(viewBlock = {
        LayoutInflater.from(it).inflate(R.layout.camera_preview, FrameLayout(it), false)
    }, modifier = modifier) {
        val view = it.findViewById<PreviewView>(R.id.viewFinder)
        cameraHandler.setup(view)
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    CleanTheme {
        CaptureScreen()
    }
}