package io.castled.notifications.workmanager

import android.content.Context
import androidx.work.*
import io.castled.notifications.logger.CastledLogger
import io.castled.notifications.logger.LogTags
import io.castled.notifications.store.CastledDbBuilder
import io.castled.notifications.store.models.NetworkRetryLog
import io.castled.notifications.workmanager.models.CastledNetworkRequest
import retrofit2.Response
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.random.Random

internal class CastledNetworkWorkManager(context: Context) {

    private val networkRetryRepository =
        NetworkRetryRepository(CastledDbBuilder.getDbInstance(context).networkRetryLogDao())

    private val workManager = WorkManager.getInstance(context)

    private val constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

    private suspend fun enqueueRequest(request: CastledNetworkRequest) {
        networkRetryRepository.insertRetryRequest(NetworkRetryLog(request = request))
        val workRequest = OneTimeWorkRequestBuilder<CastledRequestWorker>()
            .addTag(CASTLED_RETRY_WORK)
            .setConstraints(constraints)
            .setInitialDelay(Random.nextLong(1, 10), TimeUnit.SECONDS)
            .setBackoffCriteria(
                BackoffPolicy.LINEAR,
                OneTimeWorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .build()
        logger.debug("enqueuing work-id: ${workRequest.id}")
        workManager.enqueue(workRequest)
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
            }
        } catch (e: IOException) {
            logger.error("Network error!", e)
            enqueueRequest(request)
        } catch (e: Exception) {
            logger.error("Unknown error!", e)
            enqueueRequest(request)
        }
    }

    companion object {

        private const val CASTLED_RETRY_WORK = "castled_network_retry_work"

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