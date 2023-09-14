package io.castled.android.notifications.workmanager.trackevents.service
import io.castled.android.notifications.workmanager.models.CastledTrackEventRequest
import retrofit2.Response
import retrofit2.http.*

internal interface TrackEventApi {

    @POST("v1/product-events/app/lists")
    suspend fun reportEvent(
        @HeaderMap headers: Map<String, String>,
        @Body request: CastledTrackEventRequest,

    ): Response<Void?>
}
