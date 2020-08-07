package com.lukma.android.features.capture

import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
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
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.lukma.android.R
import com.lukma.android.common.UiState
import com.lukma.android.common.createFile
import com.lukma.android.common.getOutputDirectory
import com.lukma.android.common.onFailure
import com.lukma.android.common.ui.DelayedSnackbar
import com.lukma.android.ui.NavigationHandlerAmbient
import com.lukma.android.ui.Screen
import com.lukma.android.ui.theme.CleanTheme
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

private var viewFinder: PreviewView? = null
private var cameraProvider: ProcessCameraProvider? = null
private var lensFacing: Int? = null
private var imageCapture: ImageCapture? = null
private var camera: Camera? = null
private var cameraExecutor: ExecutorService? = null

@Composable
fun CaptureScreen() {
    val context = ContextAmbient.current
    val lifecycleOwner = LifecycleOwnerAmbient.current

    val viewModel = viewModel<CaptureViewModel>()
    val createPostState by viewModel.createPostResult.observeAsState(initial = UiState.None)

    if (createPostState is UiState.Success) {
        val navigation = NavigationHandlerAmbient.current
        navigation.navigateTo(Screen.Home).also { viewModel.clearState() }
    }

    launchInComposition {
        lensFacing = CameraSelector.LENS_FACING_BACK
        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    ConstraintLayout(Modifier.fillMaxSize()) {
        val (captureButton, flipButton, flashButton, errorMessage) = createRefs()

        CameraPreview(lifecycleOwner = lifecycleOwner, modifier = Modifier.fillMaxSize())
        FloatingActionButton(
            onClick = {
                val imageCapture = imageCapture ?: return@FloatingActionButton
                val cameraExecutor = cameraExecutor ?: return@FloatingActionButton

                val outputDirectory = getOutputDirectory(context)
                val file = createFile(outputDirectory, ".jpg")
                val outputOptions = ImageCapture.OutputFileOptions.Builder(file)
                    .build()

                imageCapture.takePicture(
                    outputOptions,
                    cameraExecutor,
                    object : ImageCapture.OnImageSavedCallback {
                        override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                            viewModel.createPost(file.path)
                        }

                        override fun onError(exception: ImageCaptureException) {
                            viewModel.takePictureFailed()
                        }
                    })
            },
            modifier = Modifier.constrainAs(captureButton) {
                bottom.linkTo(parent.bottom, margin = 16.dp)
                end.linkTo(parent.end)
                start.linkTo(parent.start)
            },
            backgroundColor = Color.White
        ) { }

        if (cameraProvider?.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA) == true) {
            val isBackCamera = savedInstanceState { true }
            IconButton(onClick = {
                isBackCamera.value = !isBackCamera.value

                lensFacing = if (isBackCamera.value) {
                    CameraSelector.LENS_FACING_BACK
                } else {
                    CameraSelector.LENS_FACING_FRONT
                }

                setUpCamera(lifecycleOwner)
            }, modifier = Modifier.constrainAs(flipButton) {
                bottom.linkTo(captureButton.bottom)
                end.linkTo(captureButton.start, margin = 8.dp)
                top.linkTo(captureButton.top)
            }) {
                Icon(
                    asset = vectorResource(id = R.drawable.ic_twotone_flip_camera_android_24),
                    tint = Color.White
                )
            }
        }

        val isFlashOff = savedInstanceState { true }
        IconButton(onClick = {
            isFlashOff.value = !isFlashOff.value
            camera?.cameraControl?.enableTorch(isFlashOff.value)
        }, modifier = Modifier.constrainAs(flashButton) {
            bottom.linkTo(captureButton.bottom)
            start.linkTo(captureButton.end, margin = 8.dp)
            top.linkTo(captureButton.top)
        }) {
            Icon(
                asset = vectorResource(id = if (isFlashOff.value) R.drawable.ic_twotone_flash_off_24 else R.drawable.ic_twotone_flash_on_24),
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
        viewFinder = null
        cameraProvider = null
        lensFacing = null
        imageCapture = null
        cameraExecutor?.shutdown()
        camera = null
        cameraExecutor = null
    }
}

@Composable
fun CameraPreview(lifecycleOwner: LifecycleOwner, modifier: Modifier) {
    AndroidView(viewBlock = {
        LayoutInflater.from(it).inflate(R.layout.camera_preview, FrameLayout(it), false)
    }, modifier = modifier) {
        val view = it.findViewById<PreviewView>(R.id.viewFinder)
        viewFinder = view
        viewFinder?.post {
            setUpCamera(lifecycleOwner)
        }
    }
}

private fun setUpCamera(lifecycleOwner: LifecycleOwner) {
    val viewFinder = viewFinder ?: return
    val cameraProviderFuture = ProcessCameraProvider.getInstance(viewFinder.context)
    cameraProviderFuture.addListener({
        cameraProvider = cameraProviderFuture.get()

        val cameraSelector = CameraSelector.Builder()
            .requireLensFacing(lensFacing ?: CameraSelector.LENS_FACING_BACK)
            .build()

        val cameraPreview = Preview.Builder()
            .build()

        imageCapture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()

        runCatching {
            cameraProvider?.unbindAll()
            camera = cameraProvider?.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                cameraPreview,
                imageCapture
            )
            cameraPreview.setSurfaceProvider(viewFinder.createSurfaceProvider())
        }
    }, ContextCompat.getMainExecutor(viewFinder.context))
}

@androidx.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    CleanTheme {
        CaptureScreen()
    }
}