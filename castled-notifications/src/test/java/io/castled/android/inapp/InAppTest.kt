package io.castled.android.inapp

import android.app.Application
import androidx.room.Room
import io.castled.android.helpers.CastledInitializer
import io.castled.android.helpers.CastledTestApplication
import io.castled.android.notifications.CastledNotifications
import io.castled.android.notifications.inapp.CampaignResponseConverter.toCampaign
import io.castled.android.notifications.inapp.InAppNotification
import io.castled.android.notifications.inapp.models.CampaignResponse
import io.castled.android.notifications.store.CastledDb
import io.castled.android.notifications.store.CastledDbBuilder
import io.castled.android.notifications.store.dao.CampaignDao
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

class InAppTest {
    private lateinit var db: CastledDb
    private var campaignDao: CampaignDao? = null
    var application: Application? = null

    @Before
    fun setUp() {
        application = CastledTestApplication.application
        db = Room.inMemoryDatabaseBuilder(
            application!!.applicationContext,
            CastledDb::class.java
        ).build()
        campaignDao = CastledDbBuilder.getDbInstance(application!!.applicationContext).campaignDao()
    }

    @After
    fun tearDown() {
        application = null
        campaignDao = null
        db.close()
    }

    @Test
    fun testAAInitialization() = runBlocking {
        CastledInitializer.initializeCastled(application!!, enableInApp = true)

    }

    @Test
    fun testAppOpened() = runBlocking {
        val campaignResponse: List<CampaignResponse> =
            Json.decodeFromString(InAppConstants.MOCK_INAPP_OBJECT)
        val liveCampaigns = campaignResponse.map { it.toCampaign() }
        campaignDao!!.dbInsertCampaigns(liveCampaigns)
        val inAppController = InAppNotification.getInAppController()
        CastledNotifications.logAppOpenedEvent(application!!.applicationContext)
        delay(300) // Adding some delay to complete the pending items insertion
        val isPresent = inAppController.getPendingListItems()
            .any { it.notificationId == InAppConstants.APP_OPENED_ID }
        assertTrue("Inapp with ID ${InAppConstants.APP_OPENED_ID} should be present", isPresent)
    }

    @Test
    fun testPageViewed() = runBlocking {
        val campaignResponse: List<CampaignResponse> =
            Json.decodeFromString(InAppConstants.MOCK_INAPP_OBJECT)
        val liveCampaigns = campaignResponse.map { it.toCampaign() }
        campaignDao!!.dbInsertCampaigns(liveCampaigns)
        val inAppController = InAppNotification.getInAppController()
        CastledNotifications.logPageViewedEvent(application!!.applicationContext, "DetailsScreen")
        delay(300) // Adding some delay to complete the pending items insertion
        val isPresent = inAppController.getPendingListItems()
            .any { it.notificationId == InAppConstants.PAGE_VIEWED_ID }
        assertTrue("Inapp with ID ${InAppConstants.PAGE_VIEWED_ID} should be present", isPresent)
    }

    @Test
    fun testCustomInapp() = runBlocking {
        val campaignResponse: List<CampaignResponse> =
            Json.decodeFromString(InAppConstants.MOCK_INAPP_OBJECT)
        val liveCampaigns = campaignResponse.map { it.toCampaign() }
        campaignDao!!.dbInsertCampaigns(liveCampaigns)
        val inAppController = InAppNotification.getInAppController()
        CastledNotifications.logCustomAppEvent(
            application!!.applicationContext,
            "added_to_cart",
            null
        )
        delay(300) // Adding some delay to complete the pending items insertion
        val isPresent = inAppController.getPendingListItems()
            .any { it.notificationId == InAppConstants.CUSTOM_INAPP_ID }
        assertTrue("Inapp with ID ${InAppConstants.CUSTOM_INAPP_ID} should be present", isPresent)
    }

    @Test
    fun testCustomInvalidInapp() = runBlocking {
        val campaignResponse: List<CampaignResponse> =
            Json.decodeFromString(InAppConstants.MOCK_INAPP_OBJECT)
        val liveCampaigns = campaignResponse.map { it.toCampaign() }
        campaignDao!!.dbInsertCampaigns(liveCampaigns)
        val inAppController = InAppNotification.getInAppController()
        CastledNotifications.logCustomAppEvent(
            application!!.applicationContext,
            "invalid_added_to_cart",
            null
        )
        delay(300) // Adding some delay to complete the pending items insertion

        val validIds = setOf(
            InAppConstants.CUSTOM_INAPP_ID,
            InAppConstants.PAGE_VIEWED_ID,
            InAppConstants.APP_OPENED_ID
        )

        // Assertion to ensure all user IDs are in the valid set
        assertTrue(
            "The list contains invalid user IDs. Expected only IDs 1, 2, or 3.",
            inAppController.getPendingListItems().all { it.notificationId in validIds }
        )
    }
}
