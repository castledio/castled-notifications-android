package io.castled.notifications.store.dao

import androidx.room.*
import io.castled.notifications.store.models.Campaign

@Dao
internal interface CampaignDao {

    @Query("SELECT * FROM campaigns")
    suspend fun dbGetCampaigns(): List<Campaign>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun dbInsertCampaigns(campaignList: List<Campaign>) : LongArray

    @Delete
    suspend fun dbDeleteAllCampaigns(campaigns: List<Campaign>): Int

    @Query("UPDATE campaigns SET times_displayed=:timeDisplayed, last_displayed_time=:lastDisplayedTime WHERE id = :id AND notification_id = :notificationId")
    suspend fun dbUpdateCampaignLastDisplayed(timeDisplayed: Long, lastDisplayedTime: Long, id: Int, notificationId: Int): Int
}