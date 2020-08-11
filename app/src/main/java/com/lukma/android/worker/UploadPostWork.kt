package com.lukma.android.worker

import android.content.Context
import android.net.Uri
import androidx.hilt.Assisted
import androidx.hilt.work.WorkerInject
import androidx.work.*
import com.lukma.android.domain.Either
import com.lukma.android.domain.post.usecase.CreatePostUseCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import java.io.File
import java.util.concurrent.TimeUnit

class UploadPostWork @WorkerInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val createPostUseCase: CreatePostUseCase,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val filePath = inputData.getString(KEY_FILE_PATH) ?: return Result.failure()
        val result = createPostUseCase
            .addParams(url = filePath)
            .invoke()

        var isComplete = false
        when (result) {
            is Either.Value -> {
                result.value.collectLatest {
                    val progressData = workDataOf(
                        KEY_FILE_PATH to Uri.fromFile(File(filePath)).toString(),
                        KEY_PROGRESS to it.progress
                    )
                    setProgress(progressData)
                    isComplete = it.progress >= 100.0
                }
            }
            is Either.Error -> return Result.failure()
        }

        while (!isComplete) {
            delay(TimeUnit.MILLISECONDS.toMillis(500))
        }

        return Result.success()
    }

    companion object {
        const val TAG = "UploadPostWork"
        const val KEY_FILE_PATH = "KEY_FILE_PATH"
        const val KEY_PROGRESS = "KEY_PROGRESS"

        fun start(context: Context, filePath: String) {
            val inputData = workDataOf(
                KEY_FILE_PATH to filePath
            )

            val request = OneTimeWorkRequestBuilder<UploadPostWork>()
                .addTag(TAG)
                .setInputData(inputData)
                .build()

            WorkManager.getInstance(context)
                .enqueueUniqueWork(filePath, ExistingWorkPolicy.REPLACE, request)
        }
    }
}
