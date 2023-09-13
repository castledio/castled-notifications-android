package io.castled.android.notifications.store.dao

import androidx.room.*
import io.castled.android.notifications.store.models.AppInbox
import io.castled.android.notifications.store.models.Campaign

@Dao
internal interface InboxDao {

    @Query("SELECT * FROM inbox")
    suspend fun dbGetInbox(): List<AppInbox>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun dbInsertInbox(inboxList: List<AppInbox>) : LongArray

    @Delete
    suspend fun dbDeleteAllInboxItems(inboxItems: List<AppInbox>): Int

//    @Query("UPDATE inbox SET times_displayed=:timeDisplayed, last_displayed_time=:lastDisplayedTime WHERE id = :id AND messageId = :notificationId")
//    suspend fun dbUpdateCampaignLastDisplayed(timeDisplayed: Long, lastDisplayedTime: Long, id: Int, notificationId: Int): Int
}