package io.castled.android.notifications.push.views

import android.content.Context
import io.castled.android.notifications.push.models.CastledPushMessage
import io.castled.android.notifications.push.views.templates.CastledDefaultNotificationBuilder

internal object PushBuilderFactory {
    fun createPushBuilder(
        context: Context,
        pushMessage: CastledPushMessage
    ): PushBaseBuilder {
        // need applicationContext for service related templates
        // return CountdownTimerProgressBarPushBuilder(context.applicationContext, pushMessage)
        return CastledDefaultNotificationBuilder(context, pushMessage)
    }
}