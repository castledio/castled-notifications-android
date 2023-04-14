package io.castled.notifications.inapp.service

import io.castled.notifications.inapp.models.CampaignResponse
import io.castled.notifications.workmanager.models.CastledInAppEventRequest
import retrofit2.Response
import retrofit2.http.*

internal interface InAppApi {

    @GET("v1/inapp/{api-key}/android/campaigns")
    suspend fun fetchLiveCampaigns(
        @Path("api-key") apikey: String,
        @Query("user") user: String?
    ): Response<List<CampaignResponse>>

    @POST("v1/inapp/{api-key}/android/event")
    suspend fun reportEvent(
        @Path("api-key") apikey: String,
        @Body request: CastledInAppEventRequest
    ): Response<Unit>
}
