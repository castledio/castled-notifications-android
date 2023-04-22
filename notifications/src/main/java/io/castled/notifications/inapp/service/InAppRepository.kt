package io.castled.notifications.inapp.service

import android.content.Context
import io.castled.notifications.network.CastledRetrofitClient
import io.castled.notifications.inapp.models.CampaignResponse
import io.castled.notifications.logger.CastledLogger
import io.castled.notifications.logger.LogTags
import io.castled.notifications.store.CastledDbBuilder
import io.castled.notifications.store.CastledSharedStore
import io.castled.notifications.store.models.Campaign
import io.castled.notifications.workmanager.CastledNetworkWorkManager
import io.castled.notifications.workmanager.models.CastledInAppEventRequest
import retrofit2.Response
import java.io.IOException

internal class InAppRepository(context: Context) {

    private val campaignDao = CastledDbBuilder.getDbInstance(context).campaignDao()
    private val logger = CastledLogger.getInstance(LogTags.IN_APP_REPOSITORY)
    private val inAppApi = CastledRetrofitClient.create(InAppApi::class.java)
    private val networkWorkManager = CastledNetworkWorkManager.getInstance(context)

    suspend fun getCampaigns(): List<Campaign> {
        return campaignDao.dbGetCampaigns()
    }

    suspend fun insertCampaignsIntoDb(campaigns: List<Campaign>): LongArray {
        return campaignDao.dbInsertCampaigns(campaigns)
    }

    suspend fun deleteDbCampaigns(campaigns: List<Campaign>): Int {
        return campaignDao.dbDeleteAllCampaigns(campaigns)
    }

    suspend fun updateCampaignDisplayStats(campaign: Campaign) {
        campaign.lastDisplayedTime = System.currentTimeMillis() / 1000
        campaign.timesDisplayed = campaign.timesDisplayed + 1
        campaignDao.dbUpdateCampaignLastDisplayed(
            campaign.timesDisplayed,
            campaign.lastDisplayedTime,
            campaign.id,
            campaign.notificationId
        )
    }

    suspend fun updateCampaignDisplayStats(
        timeDisplayed: Long,
        lastDisplayedTime: Long,
        id: Int,
        notificationId: Int
    ): Int {
        return campaignDao.dbUpdateCampaignLastDisplayed(
            timeDisplayed,
            lastDisplayedTime,
            id,
            notificationId
        )
    }

    suspend fun fetchLiveCampaigns(): List<CampaignResponse>? {
        try {
            val response = inAppApi.fetchLiveCampaigns(
                CastledSharedStore.getApiKey(),
                CastledSharedStore.getUserId()
            )
            return if (response.isSuccessful) {
                response.body()
            } else {
                // Handle API errors (e.g., 4xx or 5xx status codes)
                val errorMessage = response.errorBody()?.string() ?: "Unknown error"
                logger.error(errorMessage)
                null
            }
        } catch (e: IOException) {
            logger.error("Network error!", e)
        } catch (e: Exception) {
            logger.error("Unknown error!", e)
        }
        return null
    }

    suspend fun reportEvent(request: CastledInAppEventRequest) {
        networkWorkManager.apiCallWithRetry(
            request = request,
            apiCall = {
                return@apiCallWithRetry inAppApi.reportEvent(
                    CastledSharedStore.getApiKey(),
                    it as CastledInAppEventRequest
                )
            }
        )
    }

    suspend fun reportEventNoRetry(request: CastledInAppEventRequest): Response<Void?> {
        return inAppApi.reportEvent(CastledSharedStore.getApiKey(), request)
    }

}