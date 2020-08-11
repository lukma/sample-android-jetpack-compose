package com.lukma.android.common

import android.content.Context
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import java.util.concurrent.Executors

class CameraHandler(private val lifecycleOwner: LifecycleOwner) {
    private lateinit var viewFinder: PreviewView
    private var cameraProvider: ProcessCameraProvider? = null
    private var lensFacing: Int? = null
    private var imageCapture: ImageCapture? = null
    private var camera: Camera? = null
    private val cameraExecutor = Executors.newSingleThreadExecutor()

    var isTorchEnable: Boolean = false
        set(value) {
            camera?.cameraControl?.enableTorch(value)
            field = value
        }
    var isBackCamera: Boolean = true
        set(value) {
            lensFacing = if (value) {
                CameraSelector.LENS_FACING_BACK
            } else {
                CameraSelector.LENS_FACING_FRONT
            }
            bind()
            field = value
        }

    fun prepare() {
        lensFacing = CameraSelector.LENS_FACING_BACK
    }

    fun setup(view: PreviewView) {
        viewFinder = view
        viewFinder.post {
            bind()
        }
    }

    fun isSupportFrontCamera() =
        cameraProvider?.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA) == true

    private fun bind() {
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

    fun takePicture(context: Context, onSuccess: (String) -> Unit, onError: () -> Unit) {
        val imageCapture = imageCapture ?: return

        val outputDirectory = getOutputDirectory(context)
        val file = createFile(outputDirectory, ".jpg")
        val outputOptions = ImageCapture.OutputFileOptions.Builder(file)
            .build()

        imageCapture.takePicture(
            outputOptions,
            cameraExecutor,
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    onSuccess(file.toString())
                }

                override fun onError(exception: ImageCaptureException) {
                    onError()
                }
            })
    }

    fun destroy() {
        cameraExecutor?.shutdown()
    }
}
