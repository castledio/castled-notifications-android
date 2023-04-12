package io.castled.notifications.workmanager

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

internal class CastledNetworkRequestWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val v = runAttemptCount
        return Result.success()
    }
}