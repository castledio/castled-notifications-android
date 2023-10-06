package io.castled.android.notifications.workmanager

import android.content.Context
import io.castled.android.notifications.inbox.viewmodel.InboxRepository
import io.castled.android.notifications.store.models.NetworkRetryLog
import io.castled.android.notifications.workmanager.models.CastledInboxEvent
import io.castled.android.notifications.workmanager.models.CastledInboxEventRequest

internal class InboxEventRequestHandler(appContext: Context) : NetworkRequestHandler {

    private val inboxEventRepository by lazy { InboxRepository(appContext) }

    override suspend fun handleRequest(
        requests: List<NetworkRetryLog>,
        onSuccess: (entries: List<NetworkRetryLog>) -> Unit,
        onError: (entries: List<NetworkRetryLog>) -> Unit
    ) {
        val batchedEvents = mutableListOf<CastledInboxEvent>()
        requests.forEach { batchedEvents.addAll((it.request as CastledInboxEventRequest).events) }
        try {
            val response =
                inboxEventRepository.reportEventNoRetry(CastledInboxEventRequest(batchedEvents))
            if (!response.isSuccessful) {
                onError(requests)
            } else {
                onSuccess(requests)
            }
        } catch (e: Exception) {
            onError(requests)
        }
    }
}