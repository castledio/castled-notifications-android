package io.castled.inAppTriggerEvents.database

import androidx.lifecycle.LiveData
import io.castled.inAppTriggerEvents.models.CampaignModel

internal class CampaignDatabaseHelperImpl(private val campaignDatabase: CampaignDatabase) :
    CampaignDatabaseHelper {

    override suspend fun getCampaignsFromDb(): List<CampaignModel> =
        campaignDatabase.campaignDao().dbGetCampaigns()

    override suspend fun getLiveDataCampaignsFromDb(): LiveData<List<CampaignModel>> =
        campaignDatabase.campaignDao().dbGetLiveDataCampaigns()


    override suspend fun insertCampaignsIntoDb(campaigns: List<CampaignModel>): LongArray =
        campaignDatabase.campaignDao().dbInsertCampaigns(campaigns)

    override suspend fun deleteDbCampaigns(): Int =
        campaignDatabase.campaignDao().dbDeleteAllCampaigns()

    override suspend fun updateDbCampaignLastDisplayed(campaign: CampaignModel) =
        campaignDatabase.campaignDao().dbUpdateCampaignLastDisplayed(campaign)

    override suspend fun updateDbCampaignLastDisplayed(
        timesDisplayed: Long,
        lastDisplayedTime: Long,
        id: Int,
        notificationId: Int
    ): Int = campaignDatabase.campaignDao().dbUpdateCampaignLastDisplayed(timesDisplayed, lastDisplayedTime, id, notificationId)


}