package io.castled.inAppTriggerEvents.requests

import io.castled.inAppTriggerEvents.models.TriggerEventModel
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

internal interface ApiInterface {
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
    ): Response<List<TriggerEventModel>>
}
