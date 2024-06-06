package io.castled.android.notifications.workmanager

import android.content.Context
import io.castled.android.notifications.commons.extenstions.isSuccessfulOrIgnoredError
import io.castled.android.notifications.push.service.PushRepository
import io.castled.android.notifications.store.models.NetworkRetryLog
import io.castled.android.notifications.workmanager.models.CastledPushEvent
import io.castled.android.notifications.workmanager.models.CastledPushEventRequest

internal class PushEventRequestHandler(appContext: Context) : NetworkRequestHandler {

    private val pushRepository by lazy { PushRepository(appContext) }

    override suspend fun handleRequest(
        requests: List<NetworkRetryLog>,
        onSuccess: (entries: List<NetworkRetryLog>) -> Unit,
        onError: (entries: List<NetworkRetryLog>) -> Unit
    ) {
        val batchedEvents = mutableListOf<CastledPushEvent>()
        requests.forEach { batchedEvents.addAll((it.request as CastledPushEventRequest).events) }
        try {
            val response = pushRepository.reportEventNoRetry(CastledPushEventRequest(batchedEvents))
            if (!response.isSuccessfulOrIgnoredError()) {
                onError(requests)
            } else {
                onSuccess(requests)
            }
        } catch (e: Exception) {
            onError(requests)
        }
    }
}