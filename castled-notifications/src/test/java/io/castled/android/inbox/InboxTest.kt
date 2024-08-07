package io.castled.android.inbox

import android.app.Application
import androidx.room.Room
import io.castled.android.helpers.CastledInitializer
import io.castled.android.helpers.CastledTestApplication
import io.castled.android.inbox.constants.InboxConstants
import io.castled.android.notifications.CastledNotifications
import io.castled.android.notifications.inbox.model.CastledInboxItem
import io.castled.android.notifications.inbox.model.InboxResponseConverter.toInbox
import io.castled.android.notifications.store.CastledDb
import io.castled.android.notifications.store.CastledDbBuilder
import io.castled.android.notifications.store.dao.InboxDao
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.junit.After
import org.junit.Before
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [31])
@FixMethodOrder(MethodSorters.NAME_ASCENDING)

class InboxTest {
    private lateinit var db: CastledDb
    private var inboxDao: InboxDao? = null
    private var application: Application? = null

    @Before
    fun setUp() {
        application = CastledTestApplication.application
        CastledInitializer.initializeCastled(application!!)
        db = Room.inMemoryDatabaseBuilder(
            application!!.applicationContext,
            CastledDb::class.java
        ).build()
        inboxDao = CastledDbBuilder.getDbInstance(application!!.applicationContext).inboxDao()
    }

    @After
    fun tearDown() {
        application = null
        inboxDao = null
        db.close()
    }

    @Test
    fun testInboxFetchOperations() = runBlocking {
        delay(300)
        val liveInboxResponse: List<CastledInboxItem> =
            Json.decodeFromString(InboxConstants.MOCK_INBOX_OBJECT)
        val liveInboxItems = liveInboxResponse.map { it.toInbox() }
        inboxDao!!.dbInsertInbox(liveInboxItems)
        var unreadCount = 0
        var inboxItemsCount = 0
        delay(300)
        //adding this delay to set the preference userid intitialization
        CastledNotifications.getInboxUnreadCount { count ->
            unreadCount = count
        }
        CastledNotifications.getInboxItems { items ->
            inboxItemsCount = items.size
        }
        delay(300)
        assertTrue(
            "Unread count from db should match with mock unread count",
            liveInboxResponse.filter { !it.read }.size == unreadCount
        )
        assertTrue(
            "Inbox items count from db should match with mock items count",
            liveInboxResponse.size == inboxItemsCount
        )
    }

    @Test
    fun testInboxInsertion() = runBlocking {
        val liveInboxResponse: List<CastledInboxItem> =
            Json.decodeFromString(InboxConstants.MOCK_INBOX_OBJECT)
        val liveInboxItems = liveInboxResponse.map { it.toInbox() }
        inboxDao!!.dbInsertInbox(liveInboxItems)
        var unreadCount = 0
        CastledNotifications.getInboxUnreadCount { count ->
            unreadCount = count
        }
        delay(300)
        assertTrue(
            "Unread count from db should match with mock unread count",
            liveInboxResponse.filter { !it.read }.size == unreadCount
        )
    }

    @Test
    fun testGetInboxUnreadCount() = runBlocking {
        clearAllItems()
        val liveInboxResponse: List<CastledInboxItem> =
            Json.decodeFromString(InboxConstants.MOCK_INBOX_OBJECT)
        val liveInboxItems = liveInboxResponse.map { it.toInbox() }
        inboxDao!!.dbInsertInbox(liveInboxItems)
        var unreadCount = 0
        CastledNotifications.getInboxUnreadCount { count ->
            unreadCount = count
        }
        delay(300)
        assertTrue(
            "Unread count from db should match with mock unread count",
            liveInboxResponse.filter { !it.read }.size == unreadCount
        )
    }

    @Test
    fun testMarkInboxItemsRead() = runBlocking {
        clearAllItems()
        val liveInboxResponse: List<CastledInboxItem> =
            Json.decodeFromString(InboxConstants.MOCK_INBOX_OBJECT)
        val liveInboxItems = liveInboxResponse.map { it.toInbox() }
        inboxDao!!.dbInsertInbox(liveInboxItems)
        val unreadInboxItems =
            inboxDao!!.getInboxObjectsByMessageIds(liveInboxResponse.filter { !it.read }
                .map { it.messageId })
        CoroutineScope(Dispatchers.IO).launch {
            unreadInboxItems.forEach {
                it.isRead = true
                inboxDao!!.updateInboxItem(it)
            }
        }
        delay(300)
        var unreadCount = 0
        CastledNotifications.getInboxUnreadCount { count ->
            unreadCount = count
        }
        delay(300)
        assertTrue(
            "Unread count from db should match with mock unread count",
            unreadCount == 0
        )
    }

    @Test
    fun testRemoveInboxItem() = runBlocking {
        clearAllItems()
        val liveInboxResponse: List<CastledInboxItem> =
            Json.decodeFromString(InboxConstants.MOCK_INBOX_OBJECT)
        val liveInboxItems = liveInboxResponse.map { it.toInbox() }
        inboxDao!!.dbInsertInbox(liveInboxItems)

        var dbItemsCount = 0
        CoroutineScope(Dispatchers.IO).launch {
            val item = inboxDao!!.getInboxObjectByMessageId(liveInboxItems.first().messageId)
            item!!.isDeleted = true
            inboxDao!!.updateInboxItem(item)
            dbItemsCount = inboxDao!!.dbGetAllInboxItems().size
        }
        delay(300)
        assertTrue(
            "Count after changing delete flag doesn't matches",
            dbItemsCount == liveInboxResponse.size - 1
        )
        CoroutineScope(Dispatchers.IO).launch {
            val item = inboxDao!!.getInboxObjectByMessageId(liveInboxItems.last().messageId)
            inboxDao!!.delete(item!!)
            dbItemsCount = inboxDao!!.dbGetAllInboxItems().size
        }
        delay(300)
        assertTrue(
            "Count after hard delete  doesn't matches",
            dbItemsCount == liveInboxResponse.size - 2
        )
    }

    private suspend fun clearAllItems() {
        val cachedInboxItems = inboxDao!!.dbGetInbox()
        inboxDao!!.dbDeleteAllInboxItems(cachedInboxItems)
        assertTrue("DB items count should be 0", inboxDao!!.dbGetInbox().isEmpty())
    }
}
