package io.castled.inAppTriggerEvents.requests

import com.google.gson.JsonObject
import io.castled.inAppTriggerEvents.models.TriggerEventModel
import retrofit2.Response
import retrofit2.http.*

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



    /*
    * Sample URL:-
    * https://test.castled.io/backend/v1/inapp/test-3b229735-04ae-455f-a5d4-20a89c092927/android/event
    *
    * Body(raw):-
    * {
    "teamId" : 1,
    "eventType" : "CLICKED",
    "btnLabel" : "primary",
    "actionType" : "DEEP_LINKING",
    "actionUri" : "https://google.com/link1",
    "sourceContext" : "SOURCE_CONTEXT",

    "ts" : 1271727783,
    "tz" : "EST"
    }
    * */
    @POST("backend/v1/inapp/{api-key}/android/event")
    suspend fun logEventView(
        @Path("api-key")
        apikey: String,
        @Body
        body: JsonObject
    ): Response<Unit>
}
