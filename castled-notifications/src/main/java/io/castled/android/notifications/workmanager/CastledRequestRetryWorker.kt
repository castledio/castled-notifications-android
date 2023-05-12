package io.castled.android.notifications.workmanager

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import io.castled.android.notifications.globals.CastledGlobals
import io.castled.android.notifications.workmanager.models.CastledNetworkRequestType
import io.castled.android.notifications.logger.CastledLogger
import io.castled.android.notifications.logger.LogTags
import io.castled.android.notifications.store.models.NetworkRetryLog
import kotlinx.coroutines.sync.withLock

internal class CastledRequestRetryWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    private val logger = CastledLogger.getInstance(LogTags.RETRY_WORKER)

    private val requestHandlerRegistry = mapOf(
        CastledNetworkRequestType.PUSH_REGISTER to PushRegisterRequestHandler(appContext),
        CastledNetworkRequestType.PUSH_EVENT to PushEventRequestHandler(appContext),
        CastledNetworkRequestType.IN_APP_EVENT to InAppEventRequestHandler(appContext)
    )

    private val networkRetryRepository = NetworkRetryRepository(appContext)

    override suspend fun doWork(): Result {
        val failedRequests = mutableListOf<NetworkRetryLog>()
        val processedRequests = mutableListOf<NetworkRetryLog>()
        val retryRequests = mutableListOf<NetworkRetryLog>()

        try {
            // Synchronize the database operation using the Mutex
            CastledGlobals.retryDbMutex.withLock {
                retryRequests.addAll(networkRetryRepository.getRetryRequests())
                val requestsByType = retryRequests.groupBy { it.request.requestType }
                requestsByType.forEach {
                    requestHandlerRegistry[it.key]!!.handleRequest(it.value,
                        onSuccess = { entries ->
                            processedRequests.addAll(entries)
                        },
                        onError = { entries ->
                            failedRequests.addAll(entries)
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
}