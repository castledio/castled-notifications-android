package io.castled.android.notifications.workmanager

import android.content.Context
import io.castled.android.notifications.commons.extenstions.isSuccessfulOrIgnoredError
import io.castled.android.notifications.store.models.NetworkRetryLog
import io.castled.android.notifications.tracking.events.service.TrackEventRepository
import io.castled.android.notifications.workmanager.models.CastledUserTrackingEventRequest

internal class UserTrackingRequestHandler(appContext: Context) : NetworkRequestHandler {

    private val trackEventRepository by lazy { TrackEventRepository(appContext) }

    override suspend fun handleRequest(
        requests: List<NetworkRetryLog>,
        onSuccess: (entries: List<NetworkRetryLog>) -> Unit,
        onError: (entries: List<NetworkRetryLog>) -> Unit
    ) {
        for (entry in requests) {
            try {
                val response =
                    trackEventRepository.reportUserTrackingEventNoRetry((entry.request as CastledUserTrackingEventRequest))
                if (!response.isSuccessfulOrIgnoredError()) {
                    onError(listOf(entry))
                } else {
                    onSuccess(listOf(entry))
                }
            } catch (e: Exception) {
                onError(listOf(entry))
            }
        }
    }
}