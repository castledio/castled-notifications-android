package io.castled.notifications.workmanager

import android.content.Context
import androidx.work.*
import io.castled.notifications.inapp.ChannelType
import io.castled.notifications.store.CastledDbBuilder
import io.castled.notifications.store.models.NetworkRetryLog
import io.castled.notifications.workmanager.models.CastledNetworkRequest
import java.util.concurrent.TimeUnit
import kotlin.random.Random

internal class CastledNetworkWorkManager(context: Context) {

    private val networkRetryRepository = NetworkRetryRepository(CastledDbBuilder.getDbInstance(context).networkRetryLogDao())

    private val workManager = WorkManager.getInstance(context)

    private val constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

    private val workRequest = OneTimeWorkRequestBuilder<CastledNetworkRequestWorker>()
        .setConstraints(constraints)
        .setInitialDelay(Random.nextLong(0, 20), TimeUnit.SECONDS)
        .setBackoffCriteria(
            BackoffPolicy.LINEAR,
            OneTimeWorkRequest.MIN_BACKOFF_MILLIS,
            TimeUnit.MILLISECONDS
        )
        .build()

    suspend fun enqueueRequest(type : ChannelType, request: CastledNetworkRequest) {
        networkRetryRepository.putRetryLog(NetworkRetryLog(channel = type.toString(), request = request))
        workManager.enqueue(workRequest)
    }

}