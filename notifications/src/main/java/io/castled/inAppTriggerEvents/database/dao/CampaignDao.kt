package io.castled.inAppTriggerEvents.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import io.castled.inAppTriggerEvents.models.CampaignModel

@Dao
internal interface CampaignDao {

    @Query("SELECT * FROM campaign")
    fun dbGetCampaigns(): List<CampaignModel>

    @Query("SELECT * FROM campaign")
    fun dbGetLiveDataCampaigns(): LiveData<List<CampaignModel>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun dbInsertCampaigns(campaignList: List<CampaignModel>) : LongArray

    @Query("DELETE FROM campaign")
    fun dbDeleteAllCampaigns(): Int

    @Update
    fun dbUpdateCampaignLastDisplayed(campaignModel: CampaignModel)


    @Query("UPDATE campaign SET times_displayed=:timeDisplayed, last_displayed_time=:lastDisplayedTime WHERE id = :id AND notification_id = :notificationId")
    fun dbUpdateCampaignLastDisplayed(timeDisplayed: Long, lastDisplayedTime: Long, id: Int, notificationId: Int): Int
}