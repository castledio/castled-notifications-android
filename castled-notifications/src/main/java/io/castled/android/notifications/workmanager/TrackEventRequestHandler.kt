package io.castled.android.notifications.workmanager

import android.content.Context
import io.castled.android.notifications.commons.extenstions.isSuccessfulOrIgnoredError
import io.castled.android.notifications.store.models.NetworkRetryLog
import io.castled.android.notifications.tracking.events.service.TrackEventRepository
import io.castled.android.notifications.workmanager.models.CastledTrackEvent
import io.castled.android.notifications.workmanager.models.CastledTrackEventRequest

internal class TrackEventRequestHandler(appContext: Context) : NetworkRequestHandler {

    private val trackEventRepository by lazy { TrackEventRepository(appContext) }

    override suspend fun handleRequest(
        requests: List<NetworkRetryLog>,
        onSuccess: (entries: List<NetworkRetryLog>) -> Unit,
        onError: (entries: List<NetworkRetryLog>) -> Unit
    ) {
        val batchedEvents = mutableListOf<CastledTrackEvent>()
        requests.forEach { batchedEvents.addAll((it.request as CastledTrackEventRequest).events) }
        try {
            val response =
                trackEventRepository.reportCustomEventNoRetry(CastledTrackEventRequest(batchedEvents))
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