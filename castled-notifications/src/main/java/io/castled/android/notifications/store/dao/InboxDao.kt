package io.castled.android.notifications.store.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import io.castled.android.notifications.store.models.Inbox

@Dao
internal interface InboxDao {

    @Query("SELECT * FROM inbox ORDER BY date_added DESC")
    fun dbGetInbox(): List<Inbox>

    @Query("SELECT * FROM inbox ORDER BY is_pinned DESC, date_added DESC")
    fun getInboxItems(): LiveData<List<Inbox>>

    @Query("SELECT * FROM inbox WHERE message_id = :messageId LIMIT 1")
    fun getInboxObjectByMessageId(messageId: Long): Inbox?

    @Query("SELECT * FROM inbox WHERE message_id IN (:messageIds)")
    fun getInboxObjectsByMessageIds(messageIds: List<Long>): List<Inbox>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun dbInsertInbox(inboxList: List<Inbox>): LongArray

    @Delete
    suspend fun dbDeleteAllInboxItems(inboxItems: List<Inbox>): Int

    @Delete
    fun delete(item: Inbox)

    @Update
    fun updateInboxItem(item: Inbox)

}