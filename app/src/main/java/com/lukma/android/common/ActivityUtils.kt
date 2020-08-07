package com.lukma.android.common

import android.app.Activity
import android.content.Intent
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.runtime.ambientOf

class ActivityResultHandler(
    private val activityResultLauncher: ((ActivityResult) -> Unit) -> ActivityResultLauncher<Intent>
) {
    fun launch(intent: Intent, onResultOk: (Intent?) -> Unit) {
        val callback: (ActivityResult) -> Unit = {
            if (it.resultCode == Activity.RESULT_OK) {
                onResultOk(it.data)
            }
        }
        activityResultLauncher(callback).launch(intent)
    }
}

val ActivityResultHandlerAmbient =
    ambientOf<ActivityResultHandler> { error("Activity result handler not initialized") }
