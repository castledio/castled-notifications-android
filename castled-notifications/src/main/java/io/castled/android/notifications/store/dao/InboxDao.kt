package io.castled.android.notifications.store.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import io.castled.android.notifications.store.models.AppInbox

@Dao
internal interface InboxDao {

    @Query("SELECT * FROM inbox ORDER BY date_added DESC")
    fun dbGetInbox(): List<AppInbox>

    @Query("SELECT * FROM inbox ORDER BY is_pinned DESC, date_added DESC")
    fun getInboxItems(): LiveData<List<AppInbox>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun dbInsertInbox(inboxList: List<AppInbox>): LongArray

    @Delete
    suspend fun dbDeleteAllInboxItems(inboxItems: List<AppInbox>): Int

    @Delete
    fun delete(item: AppInbox)

    @Update
    fun updateInboxItem(item: AppInbox)

//    @Query("UPDATE inbox SET times_displayed=:timeDisplayed, last_displayed_time=:lastDisplayedTime WHERE id = :id AND messageId = :notificationId")
//    suspend fun dbUpdateCampaignLastDisplayed(timeDisplayed: Long, lastDisplayedTime: Long, id: Int, notificationId: Int): Int
}