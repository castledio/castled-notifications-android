package io.castled.android.notifications.inbox.viewmodel

import io.castled.android.notifications.inbox.model.InboxResponse
import io.castled.android.notifications.workmanager.models.CastledInboxEventRequest
import retrofit2.Response
import retrofit2.http.*

internal interface InboxApi {

    @GET("v1/app-inbox/{api-key}/ios/campaigns")
    suspend fun fetchInboxItems(
        @Path("api-key") apikey: String,
        @Query("user") user: String?
    ): Response<List<InboxResponse>>

    @POST("v1/app-inbox/{api-key}/ios/event")
    suspend fun reportInboxEvent(
        @Path("api-key") apikey: String,
        @Body request: CastledInboxEventRequest
    ): Response<Void?>
}
