package io.castled.android.notifications.tracking.events.service

import io.castled.android.notifications.workmanager.models.CastledDeviceTrackingRequest
import retrofit2.Response
import retrofit2.http.*

internal interface TrackDeviceApi {

    @POST("external/v1/collections/devices")
    suspend fun reportDeviceTracking(
        @HeaderMap headers: Map<String, String>,
        @Body request: CastledDeviceTrackingRequest,
    ): Response<Void?>
}
