package io.castled.android.notifications.sessions.service

import io.castled.android.notifications.workmanager.models.CastledSessionRequest
import retrofit2.Response
import retrofit2.http.*

internal interface SessionsApi {

    @POST("external/v1/collections/session-events/lists")
    suspend fun reportEvent(@Body request: CastledSessionRequest): Response<Void?>
}
