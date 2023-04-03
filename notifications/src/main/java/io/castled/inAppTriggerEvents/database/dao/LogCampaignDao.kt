package io.castled.inAppTriggerEvents.database.dao

import androidx.room.*
import io.castled.inAppTriggerEvents.models.LogCampaignModel


@Dao
internal interface LogCampaignDao {

    @Query("SELECT * FROM log_campaign")
    fun dbGetLogCampaigns(): List<LogCampaignModel>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun dbInsertLogCampaigns(logCampaignList: List<LogCampaignModel>): LongArray

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun dbInsertLogCampaign(logCampaignDao: LogCampaignModel): Long

    @Delete
    suspend fun dbDeleteLogCampaign(logCampaign: LogCampaignModel)

//    val logEventDate = System.currentTimeMillis()
}