package io.castled.inAppTriggerEvents.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.castled.inAppTriggerEvents.models.CampaignModel
import io.castled.inAppTriggerEvents.models.LogCampaignModel


@Dao
internal interface LogCampaignDao {

    @Query("SELECT * FROM log_campaign")
    fun dbGetLogCampaigns(): List<LogCampaignModel>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun dbInsertLogCampaigns(logCampaignList: List<LogCampaignModel>): LongArray

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun dbInsertLogCampaign(logCampaignDao: LogCampaignModel): Long

//    val logEventDate = System.currentTimeMillis()
}