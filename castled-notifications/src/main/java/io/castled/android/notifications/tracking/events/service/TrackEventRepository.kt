package io.castled.android.notifications.tracking.events.service

import android.content.Context
import io.castled.android.notifications.network.CastledRetrofitClient
import io.castled.android.notifications.workmanager.CastledNetworkWorkManager
import io.castled.android.notifications.workmanager.models.CastledTrackEventRequest
import io.castled.android.notifications.workmanager.models.CastledUserTrackingEventRequest
import retrofit2.Response

internal class TrackEventRepository(context: Context) {

    private val trackEventApi by lazy { CastledRetrofitClient.create(TrackEventApi::class.java) }
    private val networkWorkManager by lazy { CastledNetworkWorkManager.getInstance(context) }

    suspend fun reportCustomEvent(request: CastledTrackEventRequest) {
        networkWorkManager.apiCallWithRetry(request = request, apiCall = {
            return@apiCallWithRetry trackEventApi.reportEvent(
                (it as CastledTrackEventRequest)
            )
        })
    }

    suspend fun reportCustomEventNoRetry(request: CastledTrackEventRequest): Response<Void?> {
        return trackEventApi.reportEvent(
            request
        )
    }

    suspend fun reportUserTrackingEvent(request: CastledUserTrackingEventRequest) {
        networkWorkManager.apiCallWithRetry(request = request, apiCall = {
            return@apiCallWithRetry trackEventApi.reportUserTrackingEvent(
                (it as CastledUserTrackingEventRequest)
            )
        })
    }

    suspend fun reportUserTrackingEventNoRetry(request: CastledUserTrackingEventRequest): Response<Void?> {
        return trackEventApi.reportUserTrackingEvent(
            request
        )
    }
}