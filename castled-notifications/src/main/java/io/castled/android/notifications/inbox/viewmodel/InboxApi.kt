package io.castled.android.notifications.inbox.viewmodel

import io.castled.android.notifications.inapp.models.CampaignResponse
import io.castled.android.notifications.inbox.model.InboxResponse
import io.castled.android.notifications.workmanager.models.CastledInAppEventRequest
import retrofit2.Response
import retrofit2.http.*

internal interface InboxApi {

    @GET("v1/app-inbox/{api-key}/android/campaigns")
    suspend fun fetchInboxItems(
        @Path("api-key") apikey: String,
        @Query("user") user: String?
    ): Response<List<InboxResponse>>

    @POST("v1/app-inbox/{api-key}/android/event")
    suspend fun reportInboxEvent(
        @Path("api-key") apikey: String,
        @Body request: CastledInAppEventRequest
    ): Response<Void?>
}
