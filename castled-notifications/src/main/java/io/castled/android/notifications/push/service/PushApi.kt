package io.castled.android.notifications.push.service

import io.castled.android.notifications.push.models.CastledPushMessage
import io.castled.android.notifications.workmanager.models.CastledLogoutRequest
import io.castled.android.notifications.workmanager.models.CastledPushEventRequest
import io.castled.android.notifications.workmanager.models.CastledPushRegisterRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

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

    @GET("v1/push/{apiKey}/android/messages")
    suspend fun getMessages(
        @Path("apiKey") apiKey: String,
        @Query("user") user: String?
    ): Response<List<CastledPushMessage>>

    @PUT("v1/push/{apiKey}/fcm/logout")
    suspend fun logout(
        @Path("apiKey") apiKey: String,
        @Body logoutRequest: CastledLogoutRequest
    ): Response<Void?>
}