package io.castled.inappNotifications.requests

import io.castled.inappNotifications.models.NotificationModel
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiInterface {
    /*
    * Sample URL:-
    * https://test.castled.io/backend/v1/inapp/<api-key>/android/campaigns?user=support-1@castled.io
    * */
    @GET("backend/v1/inapp/{api-key}/android/campaigns")
    suspend fun makeNotificationQuery(
        @Path("api-key")
        apikey: String,
        @Query("user")
        customers: String
    ): Response<List<NotificationModel>>
}
