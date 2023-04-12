package io.castled.notifications.push.service

import io.castled.notifications.push.models.FcmDeviceRegisterRequest
import io.castled.notifications.push.models.NotificationEvent
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

interface PushApi {
    @POST("v1/push/{apiKey}/fcm/register")
    suspend fun register(
        @Path("apiKey") apiKey: String,
        @Body fcmDeviceRegisterRequest: FcmDeviceRegisterRequest
    ): Response<Void?>

    @POST("v1/push/{apiKey}/event")
    suspend fun reportEvent(
        @Path("apiKey") apiKey: String,
        @Body notificationEvent: NotificationEvent
    ): Response<Void?>
}