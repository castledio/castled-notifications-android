package io.castled.notifications.workmanager

import android.content.Context
import io.castled.notifications.inapp.service.InAppRepository
import io.castled.notifications.store.models.NetworkRetryLog
import io.castled.notifications.workmanager.models.CastledInAppEvent
import io.castled.notifications.workmanager.models.CastledInAppEventRequest

internal class InAppEventRequestHandler(appContext: Context) : NetworkRequestHandler {

    private val inAppRepository by lazy { InAppRepository(appContext) }

    override suspend fun handleRequest(
        requests: List<NetworkRetryLog>,
        onSuccess: (entries: List<NetworkRetryLog>) -> Unit,
        onError: (entries: List<NetworkRetryLog>) -> Unit
    ) {
        val batchedEvents = mutableListOf<CastledInAppEvent>()
        requests.forEach { batchedEvents.addAll((it.request as CastledInAppEventRequest).events) }
        try {
            val response =
                inAppRepository.reportEventNoRetry(CastledInAppEventRequest(batchedEvents))
            if (!response.isSuccessful) {
                onError(requests)
            }
        } catch (e: Exception) {
            onError(requests)
        }
        onSuccess(requests)
    }
}