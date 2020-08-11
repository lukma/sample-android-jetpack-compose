package com.lukma.android.common

import android.content.Context
import androidx.compose.runtime.ambientOf
import androidx.lifecycle.LiveData
import androidx.work.WorkInfo
import androidx.work.WorkManager

class WorkerWatcher(context: Context) {
    private val worker = WorkManager.getInstance(context)
    var workInfo: LiveData<WorkInfo>? = null

    fun watch(tag: String) {
        val workId = worker.getWorkInfosByTag(tag).get().lastOrNull()?.id
        if (workId != null) {
            workInfo = worker.getWorkInfoByIdLiveData(workId)
        }
    }
}

val WorkerWatcherAmbient = ambientOf<WorkerWatcher> { error("work watcher not initialized") }
