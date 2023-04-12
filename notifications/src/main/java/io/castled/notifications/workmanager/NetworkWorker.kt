package io.castled.notifications.workmanager

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class NetworkWorker(context: Context, workerParameters: WorkerParameters) : CoroutineWorker(context, workerParameters) {

    override suspend fun doWork(): Result {

        return Result.success()
    }
}