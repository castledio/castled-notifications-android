package io.castled.android.notifications.trackevents.service

import io.castled.android.notifications.workmanager.models.CastledTrackEventRequest
import retrofit2.Response
import retrofit2.http.*

internal interface TrackEventApi {

    @POST("external/v1/collections/events/lists?apiSource=app")
    suspend fun reportEvent(
        @HeaderMap headers: Map<String, String>,
        @Body request: CastledTrackEventRequest,

        ): Response<Void?>
}
