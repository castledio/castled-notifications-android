package io.castled.inAppTriggerEvents.database

import androidx.lifecycle.LiveData
import io.castled.inAppTriggerEvents.models.CampaignModel
import io.castled.inAppTriggerEvents.models.LogCampaignModel

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

    override suspend fun updateDbCampaignLastDisplayed(campaignModel: CampaignModel) =
        campaignDatabase.campaignDao().dbUpdateCampaignLastDisplayed(campaignModel)

    override suspend fun updateDbCampaignLastDisplayed(
        timeDisplayed: Long,
        lastDisplayedTime: Long,
        id: Int,
        notificationId: Int
    ): Int = campaignDatabase.campaignDao().dbUpdateCampaignLastDisplayed(timeDisplayed, lastDisplayedTime, id, notificationId)



    override suspend fun getLogCampaignFromDb(): List<LogCampaignModel> =
        campaignDatabase.logCampaignDao().dbGetLogCampaigns()

    override suspend fun insertLogCampaigns(logCampaigns: List<LogCampaignModel>): LongArray =
        campaignDatabase.logCampaignDao().dbInsertLogCampaigns(logCampaigns)

    override suspend fun insertLogCampaign(logCampaign: LogCampaignModel): Long =
            campaignDatabase.logCampaignDao().dbInsertLogCampaign(logCampaign)


}