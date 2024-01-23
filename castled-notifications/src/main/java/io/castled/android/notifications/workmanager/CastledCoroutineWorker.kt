package io.castled.android.notifications.workmanager

import android.app.Application
import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import io.castled.android.notifications.CastledNotifications

abstract class CastledCoroutineWorker(
    appContext: Context,
    workerParams: WorkerParameters
) :
    CoroutineWorker(appContext, workerParams) {

        init {
            CastledNotifications.initializeInternal(appContext.applicationContext as Application)
        }
}