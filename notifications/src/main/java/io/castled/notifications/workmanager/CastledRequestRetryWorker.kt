package io.castled.notifications.workmanager

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import io.castled.notifications.globals.CastledGlobals
import io.castled.notifications.inapp.service.InAppRepository
import io.castled.notifications.logger.CastledLogger
import io.castled.notifications.logger.LogTags
import io.castled.notifications.push.service.PushRepository
import io.castled.notifications.store.models.NetworkRetryLog
import io.castled.notifications.workmanager.models.*
import io.castled.notifications.workmanager.models.CastledPushEventRequest
import io.castled.notifications.workmanager.models.CastledPushRegisterRequest
import kotlinx.coroutines.sync.withLock

internal class CastledRequestRetryWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    private val logger = CastledLogger.getInstance(LogTags.RETRY_WORKER)

    private val pushRepository by lazy { PushRepository(appContext) }

    private val inAppRepository by lazy { InAppRepository(appContext) }

    private val networkRetryRepository = NetworkRetryRepository(appContext)

    override suspend fun doWork(): Result {
        val failedRequests = mutableListOf<NetworkRetryLog>()
        val processedRequests = mutableListOf<NetworkRetryLog>()
        val retryRequests = mutableListOf<NetworkRetryLog>()

        try {
            // Synchronize the database operation using the Mutex
            CastledGlobals.retryDbMutex.withLock {
                retryRequests.addAll(networkRetryRepository.getRetryRequests())
                retryRequests.forEach {
                    processRequest(it,
                        onSuccess = { entry ->
                            processedRequests.add(entry)
                        },
                        onError = { entry ->
                            failedRequests.add(entry)
                        })
                }
                networkRetryRepository.deleteRetryRequests(processedRequests)
            }
        } catch (e: Exception) {
            logger.error("work with id: $id failed!", e)
            // Handle any exceptions and return the appropriate result
            Result.failure()
        }
        logger.verbose(
            "total requests to retry: ${retryRequests.size}, " +
                    "processed: ${processedRequests.size}, failed: ${failedRequests.size}"
        )
        return if (failedRequests.size > 0) Result.retry() else Result.success()
    }

    private suspend fun processRequest(
        entry: NetworkRetryLog,
        onSuccess: (entry: NetworkRetryLog) -> Unit,
        onError: (entry: NetworkRetryLog) -> Unit
    ) {
        val request = entry.request
        try {
            val response = when (entry.request.requestType) {
                CastledNetworkRequestType.PUSH_REGISTER -> {
                    pushRepository.registerNoRetry(
                        (request as CastledPushRegisterRequest).userId,
                        request.fcmToken
                    )
                }
                CastledNetworkRequestType.PUSH_EVENT -> {
                    pushRepository.reportEventNoRetry(request as CastledPushEventRequest)
                }
                CastledNetworkRequestType.IN_APP_EVENT -> {
                    inAppRepository.reportEventNoRetry(
                        request as CastledInAppEventRequest
                    )
                }
            }
            if (response.isSuccessful) {
                onSuccess(entry)
            } else {
                onError(entry)
                logger.error("error body:${response.errorBody()?.string()}")
            }
        } catch (e: Exception) {
            onError(entry)
        }
    }
}