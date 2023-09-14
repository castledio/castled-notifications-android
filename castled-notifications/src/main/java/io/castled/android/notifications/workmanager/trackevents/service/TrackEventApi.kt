package io.castled.android.notifications.workmanager.trackevents.service

import io.castled.android.notifications.workmanager.models.CastledTrackEvent
import retrofit2.Response
import retrofit2.http.*

internal interface TrackEventApi {

    @POST("v1/product-events/{api-key}")
    suspend fun reportEvent(
        @Path("api-key") apikey: String,
        @Body request: CastledTrackEvent
    ): Response<Void?>
}
