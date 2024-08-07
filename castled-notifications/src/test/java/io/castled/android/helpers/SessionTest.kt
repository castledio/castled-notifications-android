package io.castled.android.helpers

import android.app.Application
import io.castled.android.notifications.sessions.Sessions
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNotNull
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertNotEquals
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

class SessionTest {
    private var application: Application? = null
    private val sessionTestDuration = 2L

    companion object {
        private var lastSessionId: String? = null
    }

    @Before
    fun setUp() {
        application = CastledTestApplication.application
        CastledInitializer.initializeCastled(application!!)
    }

    @After
    fun tearDown() {
        application = null
    }

    @Test
    fun testAStartSession() = runBlocking {
        Sessions.startCastledSession()
        delay(1000)
        lastSessionId = Sessions.sessionId
        assertNotNull("Session id hould not be null", lastSessionId)
        assertFalse("Session id should not be null/empty", lastSessionId!!.isEmpty())
    }

    @Test
    fun testEndSession() = runBlocking {
        Sessions.didEnterBackground()
        delay((sessionTestDuration + 1L) * 1000)
        val duration = Sessions.sessionDuration
        Sessions.startCastledSession()
        val newSessionId = Sessions.sessionId
        assertNotEquals("Session ids should not be equal", lastSessionId, newSessionId)
        assertNotEquals("Session duration cannot be 0", duration, 0L)
    }
}
