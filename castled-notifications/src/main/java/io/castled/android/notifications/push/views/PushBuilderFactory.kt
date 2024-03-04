package io.castled.android.notifications.push.views

import android.content.Context
import io.castled.android.notifications.push.models.CastledPushMessage
import io.castled.android.notifications.push.views.templates.TimerPushBuilder

internal object PushBuilderFactory {
    fun createPushBuilder(
        context: Context,
        pushMessage: CastledPushMessage
    ): PushBaseBuilder? {
        return TimerPushBuilder(context, pushMessage)
        // return CastledNotificationBuilder(context, pushMessage)
    }
}