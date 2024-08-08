package io.castled.android.notifications.store.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import io.castled.android.notifications.store.models.Inbox

@Dao
internal interface InboxDao {

    @Query("SELECT * FROM inbox ORDER BY date_added DESC")
    //not adding  is_deleted otherwise
    // it will get again added as there is check while inserting
    suspend fun dbGetInbox(): List<Inbox>

    @Query("SELECT * FROM inbox WHERE is_deleted = 0 ORDER BY date_added DESC")
    suspend fun dbGetAllInboxItems(): List<Inbox>


    @Query("SELECT * FROM inbox WHERE is_deleted = 0 ORDER BY is_pinned DESC, date_added DESC")
    fun getInboxItems(): LiveData<List<Inbox>>


    @Query(
        "SELECT * FROM inbox WHERE ((LENGTH(:selectedTag) > 0 AND tag = :selectedTag)" +
                " OR LENGTH(:selectedTag) = 0) AND is_deleted = 0 ORDER BY is_pinned DESC, " +
                "date_added DESC"
    )
    fun getInboxItemsWith(selectedTag: String): LiveData<List<Inbox>>


    @Query("SELECT * FROM inbox WHERE message_id = :messageId LIMIT 1")
    fun getInboxObjectByMessageId(messageId: Long): Inbox?

    @Query("SELECT * FROM inbox WHERE message_id IN (:messageIds)")
    suspend fun getInboxObjectsByMessageIds(messageIds: List<Long>): List<Inbox>

    @Query("SELECT COUNT(*) FROM inbox WHERE is_read = 0 AND is_deleted = 0")
    suspend fun getInboxUnreadCount(): Int

    @Query("SELECT DISTINCT tag FROM Inbox WHERE is_deleted = 0 AND tag IS NOT NULL AND tag <> '' ORDER BY tag ASC")
    suspend fun getUniqueNonEmptyTags(): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun dbInsertInbox(inboxList: List<Inbox>): LongArray

    @Delete
    suspend fun dbDeleteAllInboxItems(inboxItems: List<Inbox>): Int

    @Delete
    fun delete(item: Inbox)

    @Update
    fun updateInboxItem(item: Inbox)

}