package io.castled.android.notifications.tracking.events.service

import io.castled.android.notifications.workmanager.models.CastledTrackEventRequest
import io.castled.android.notifications.workmanager.models.CastledUserTrackingEventRequest
import retrofit2.Response
import retrofit2.http.*

internal interface TrackEventApi {

    @POST("external/v1/collections/events/lists?apiSource=app")
    suspend fun reportEvent(
        @HeaderMap headers: Map<String, String>,
        @Body request: CastledTrackEventRequest
    ): Response<Void?>

    @POST("external/v1/collections/users?apiSource=app")
    suspend fun reportUserTrackingEvent(
        @HeaderMap headers: Map<String, String>,
        @Body request: CastledUserTrackingEventRequest,
    ): Response<Void?>
}
