package io.castled.android.notifications.workmanager

import android.content.Context
import androidx.work.*
import io.castled.android.notifications.logger.CastledLogger
import io.castled.android.notifications.logger.LogTags
import io.castled.android.notifications.store.models.NetworkRetryLog
import io.castled.android.notifications.workmanager.models.CastledNetworkRequest
import retrofit2.Response
import java.util.concurrent.TimeUnit
import kotlin.random.Random

internal class CastledNetworkWorkManager private constructor(context: Context) {

    private val networkRetryRepository = NetworkRetryRepository(context)

    private val workManager = WorkManager.getInstance(context)

    private val constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED)
        .setRequiresBatteryNotLow(true).build()

    private suspend fun enqueueRequest(request: CastledNetworkRequest) {
        networkRetryRepository.insertRetryRequest(NetworkRetryLog(request = request))
        val workRequest =
            OneTimeWorkRequestBuilder<CastledRequestRetryWorker>().setConstraints(constraints)
                .setInitialDelay(Random.nextLong(1, 10), TimeUnit.SECONDS).setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    OneTimeWorkRequest.MIN_BACKOFF_MILLIS,
                    TimeUnit.MILLISECONDS
                ).build()
        logger.debug("enqueuing work-id: ${workRequest.id}")
        workManager.beginUniqueWork(
            CASTLED_NETWORK_RETRY_WORK, ExistingWorkPolicy.REPLACE, workRequest
        ).enqueue()
    }

    suspend fun <T> apiCallWithRetry(
        request: CastledNetworkRequest,
        apiCall: suspend (request: CastledNetworkRequest) -> Response<T>
    ) {
        try {
            val response = apiCall(request)
            if (!response.isSuccessful) {
                logger.error("error code:${response.code()} message: ${response.message()}")
                enqueueRequest(request)
            }else{
                logger.debug("api success ${request.requestType}")
            }
        } catch (e: Exception) {
            logger.error("Error making API call!", e)
            enqueueRequest(request)
        }
    }

    companion object {

        private const val CASTLED_NETWORK_RETRY_WORK = "castled_network_retry_work"

        private val logger = CastledLogger.getInstance(LogTags.WORK_MANAGER)

        private var instance: CastledNetworkWorkManager? = null

        @Synchronized
        fun getInstance(context: Context): CastledNetworkWorkManager {
            if (instance == null) {
                instance = CastledNetworkWorkManager(context)
            }
            return instance!!
        }
    }

}