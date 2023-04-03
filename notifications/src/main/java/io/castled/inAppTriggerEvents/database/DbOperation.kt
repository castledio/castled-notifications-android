package io.castled.inAppTriggerEvents.database

import android.content.Context
import com.google.gson.JsonObject
import io.castled.inAppTriggerEvents.models.CampaignModel
import io.castled.inAppTriggerEvents.models.LogCampaignModel
import io.castled.notifications.logger.CastledLogger
import io.castled.notifications.logger.LogTags
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

internal class DbOperation {
    companion object {
        private val castledLogger = CastledLogger.getInstance(LogTags.DB_OPS)
        internal fun dbUpdateCampaignLastDisplayedAndTimesDisplayed(context: Context, campaignModel: CampaignModel) {
            CoroutineScope(Dispatchers.Default).launch {
                val db = CampaignDatabaseHelperImpl(DatabaseBuilder.getInstance(context))
                campaignModel.lastDisplayedTime = System.currentTimeMillis()
                campaignModel.timesDisplayed = campaignModel.timesDisplayed + 1
//                db.updateDbCampaignLastDisplayed(campaignModel)

                val rowEffected = db.updateDbCampaignLastDisplayed(campaignModel.timesDisplayed, campaignModel.lastDisplayedTime,  campaignModel.id, campaignModel.notificationId)

                castledLogger.info("dbUpdateCampaignLastDisplayed-> $rowEffected for timesDisplayed: ${campaignModel.timesDisplayed}, lastDisplayedTime: ${campaignModel.lastDisplayedTime},  id: ${campaignModel.id}, notificationId: ${campaignModel.notificationId}")

            }
        }

        internal fun dbInsertLogCampaign(context: Context, jsonObject: JsonObject) {
            CoroutineScope(Dispatchers.Default).launch {
                val logCampaignModel = LogCampaignModel(0, jsonObject)
                val db = CampaignDatabaseHelperImpl(DatabaseBuilder.getInstance(context))
                val rowEffected = db.insertLogCampaign(logCampaignModel)
                castledLogger.info("dbInsertLogCampaign-> rowEffected: $rowEffected, logCampaignModel: $logCampaignModel")
            }
        }
    }
}