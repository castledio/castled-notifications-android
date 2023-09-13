package io.castled.android.notifications.workmanager

import android.content.Context
import io.castled.android.notifications.inapp.service.InAppRepository
import io.castled.android.notifications.store.models.NetworkRetryLog
import io.castled.android.notifications.workmanager.models.CastledInAppEvent
import io.castled.android.notifications.workmanager.models.CastledInAppEventRequest
import io.castled.android.notifications.workmanager.models.CastledTrackEvent
import io.castled.android.notifications.workmanager.models.CastledTrackEventRequest
import io.castled.android.notifications.workmanager.trackevents.service.TrackEventRepository

internal class TrackEventRequestHandler(appContext: Context) : NetworkRequestHandler {

    private val trackEventRepository by lazy { TrackEventRepository(appContext) }

    override suspend fun handleRequest(
        requests: List<NetworkRetryLog>,
        onSuccess: (entries: List<NetworkRetryLog>) -> Unit,
        onError: (entries: List<NetworkRetryLog>) -> Unit
    ) {
        requests.forEach {
            try {
                val response =
                    trackEventRepository.reportEventNoRetry(it.request as CastledTrackEventRequest)
                if (!response.isSuccessful) {
                    onError(requests)
                }
            } catch (e: Exception) {
                onError(requests)
            }
            onSuccess(listOf(it))
        }
    }
}