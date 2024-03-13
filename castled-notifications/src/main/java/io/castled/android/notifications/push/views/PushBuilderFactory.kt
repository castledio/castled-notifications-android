package io.castled.android.notifications.push.views

import android.content.Context
import io.castled.android.notifications.push.models.CastledPushMessage
import io.castled.android.notifications.push.views.templates.CastledDefaultNotificationBuilder
import kotlinx.coroutines.CoroutineScope

internal object PushBuilderFactory {
    fun createPushBuilder(
        context: Context,
        pushMessage: CastledPushMessage,
        externalScope: CoroutineScope
    ): PushBaseBuilder? {

        return CastledDefaultNotificationBuilder(context, pushMessage, externalScope)
    }
}