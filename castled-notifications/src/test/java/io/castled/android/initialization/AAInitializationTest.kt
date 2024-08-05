package io.castled.android.initialization

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import io.castled.android.helpers.CastledInitializer
import io.castled.android.helpers.CastledTestApplication
import io.castled.android.notifications.CastledNotifications
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [31])
class AAInitializationTest {
    var application: Application? = null

    @Before
    fun setUp() {
        application = CastledTestApplication.application
    }

    @After
    fun tearDown() {
        application = null
    }

    @Test
    fun testInitialization() {
        application = ApplicationProvider.getApplicationContext<Application>()
        application?.let {
            assertFalse(CastledNotifications.isInited())
            CastledInitializer.initializeCastled(it)
            assertTrue(
                "Expected value to be true but got false",
                CastledNotifications.isInited()
            )

        } ?: run {
            assertEquals("unable to initialize applicaiton", true, false)
        }
    }

}