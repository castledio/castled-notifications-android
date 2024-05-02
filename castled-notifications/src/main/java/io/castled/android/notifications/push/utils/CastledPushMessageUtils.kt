package io.castled.android.notifications.push.utils

import io.castled.android.notifications.push.models.CastledPushMessage
import io.castled.android.notifications.push.models.PushConstants

internal object CastledPushMessageUtils {
    fun CastledPushMessage.getChannelId(): String {
        return channelId.takeUnless { it.isNullOrBlank() }
            ?: PushConstants.CASTLED_DEFAULT_CHANNEL_ID
    }
}