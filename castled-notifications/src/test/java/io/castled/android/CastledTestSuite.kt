package io.castled.android

import io.castled.android.Initialization.CastledInitializationTest
import io.castled.android.inapp.InAppTest
import io.castled.android.inbox.InboxTest
import io.castled.android.notifications.trigger.TriggerEvaluatorTest
import org.junit.runner.RunWith
import org.junit.runners.Suite

@RunWith(Suite::class)
@Suite.SuiteClasses(
    CastledInitializationTest::class,
    InAppTest::class,
    InboxTest::class,
    TriggerEvaluatorTest::class,
)
class CastledTestSuite