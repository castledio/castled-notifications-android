package io.castled.android.notifications.user_life_cycle.services

import io.castled.android.notifications.workmanager.models.CastledLogoutRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.PUT
import retrofit2.http.Path

internal interface UserLifeCycleApi {
    @PUT("v1/push/{apiKey}/fcm/logout")
    suspend fun logout(
        @Path("apiKey") apiKey: String,
        @Body logoutRequest: CastledLogoutRequest
    ): Response<Void?>
}