package io.castled.android.notifications.tracking.events.service

import io.castled.android.notifications.workmanager.models.CastledTrackEventRequest
import io.castled.android.notifications.workmanager.models.CastledUserTrackingEventRequest
import retrofit2.Response
import retrofit2.http.*

internal interface TrackEventApi {

    @POST("external/v1/collections/events/lists?apiSource=app&pf=android")
    suspend fun reportEvent(
        @Body request: CastledTrackEventRequest
    ): Response<Void?>

    @POST("external/v1/collections/users?apiSource=app&pf=android")
    suspend fun reportUserTrackingEvent(
        @Body request: CastledUserTrackingEventRequest,
    ): Response<Void?>
}
