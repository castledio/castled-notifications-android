package io.castled.android.notifications.push.extensions

import io.castled.android.notifications.push.models.CastledClickAction
import io.castled.android.notifications.push.models.CastledPushMessage

fun CastledPushMessage.getTitle(): String? = title

fun CastledPushMessage.getBody(): String? = body

fun CastledPushMessage.getDefaultClickAction(): CastledClickAction? =
    pushMessageFrames[0].clickAction

fun CastledPushMessage.getDefaultActionPayload(): Map<String, String>? =
    pushMessageFrames[0].keyVals

fun CastledPushMessage.getNotificationDisplayId(): Int =
    (title + body + notificationId.toString()).hashCode()