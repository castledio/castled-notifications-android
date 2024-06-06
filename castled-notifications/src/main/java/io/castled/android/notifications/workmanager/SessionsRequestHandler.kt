package io.castled.android.notifications.workmanager

import android.content.Context
import io.castled.android.notifications.commons.extenstions.isSuccessfulOrIgnoredError
import io.castled.android.notifications.sessions.events.CastledSessionEvent
import io.castled.android.notifications.sessions.service.SessionsRepository
import io.castled.android.notifications.store.models.NetworkRetryLog
import io.castled.android.notifications.workmanager.models.CastledSessionRequest

internal class SessionsRequestHandler(appContext: Context) : NetworkRequestHandler {

    private val sessionsRepository by lazy { SessionsRepository(appContext) }

    override suspend fun handleRequest(
        requests: List<NetworkRetryLog>,
        onSuccess: (entries: List<NetworkRetryLog>) -> Unit,
        onError: (entries: List<NetworkRetryLog>) -> Unit
    ) {
        val batchedEvents = mutableListOf<CastledSessionEvent>()
        requests.forEach { batchedEvents.addAll((it.request as CastledSessionRequest).events) }
        try {
            val response =
                sessionsRepository.reportSessionEventNoRetry(CastledSessionRequest(batchedEvents))
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