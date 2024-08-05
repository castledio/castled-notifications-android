package io.castled.android.Initialization

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
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.delay
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

class CastledInitializationTest {
    private lateinit var db: CastledDb
    private var inboxDao: InboxDao? = null
    private var application: Application? = null

    @Before
    fun setUp() {
        application = CastledTestApplication.application
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
    fun testAModuleUnInitializedCheck() = runBlocking {
        val liveInboxResponse: List<CastledInboxItem> =
            Json.decodeFromString(InboxConstants.MOCK_INBOX_OBJECT)
        val liveInboxItems = liveInboxResponse.map { it.toInbox() }
        inboxDao!!.dbInsertInbox(liveInboxItems)
        var unreadCount = 0
        var inboxItemsCount = 0
        CastledNotifications.getInboxUnreadCount { count ->
            unreadCount = count
        }
        CastledNotifications.getInboxItems { items ->
            inboxItemsCount = items.size
        }
        delay(300)
        assertTrue(
            "Unread count from db should be 0  without initialization",
            unreadCount == 0
        )
        assertTrue(
            "Inbox items count from db should be 0  without initialization",
            inboxItemsCount == 0
        )

    }

    @Test
    fun testBInitialization() = runBlocking {
        assertFalse(CastledNotifications.isInited())
        CastledInitializer.initializeCastled(application!!)
        delay(300)
        assertTrue(
            "Expected value to be true but got false",
            CastledNotifications.isInited()
        )
    }
}
