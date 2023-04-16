package io.castled.notifications.push.service

import io.castled.notifications.workmanager.models.CastledPushEventRequest
import io.castled.notifications.workmanager.models.CastledPushRegisterRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

internal interface PushApi {
    @POST("v1/push/{apiKey}/fcm/register")
    suspend fun register(
        @Path("apiKey") apiKey: String,
        @Body pushRegisterRequest: CastledPushRegisterRequest
    ): Response<Void?>

    @POST("v1/push/{apiKey}/event")
    suspend fun reportEvent(
        @Path("apiKey") apiKey: String,
        @Body eventRequest: CastledPushEventRequest
    ): Response<Void?>
}