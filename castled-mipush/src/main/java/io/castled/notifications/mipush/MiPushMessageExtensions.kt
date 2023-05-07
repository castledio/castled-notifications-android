package io.castled.notifications.mipush

import com.xiaomi.mipush.sdk.MiPushMessage
import io.castled.notifications.push.models.CastledClickAction
import io.castled.notifications.push.models.CastledNotificationFieldConsts
import io.castled.notifications.push.models.CastledPushMessage
import io.castled.notifications.push.models.CastledPushPriority
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

fun MiPushMessage.toCastledPushMessage(): CastledPushMessage? {
    try {
        return CastledPushMessage(
            notificationId = notifyId,
            sourceContext = extra["srcCtx"]!!,
            teamId = extra["tId"]?.toLong()!!,
            title = title,
            body = content,
            summary = description,
            imageUrl = extra["imageUrl"],
            sound = extra["sound"],
            priority = extra["priority"]?.let { CastledPushPriority.valueOf(it) },
            clickAction = extra["clickAction"]?.let { CastledClickAction.valueOf(it) },
            clickActionUri = extra["clickActionUri"],
            keyVals = extra["keyVals"]?.let { Json.decodeFromString(it) },
            channelId = extra["channelId"],
            channelName = extra["channelName"],
            channelDescription = extra["channelDescription"],
            smallIconResourceId = extra["smallIconResourceId"],
            largeIconUri = extra["largeIconUri"],
            castledActionButtons = extra["actionButtons"]?.let { Json.decodeFromString(it) },
            ttl = extra["ttl"]?.toLong()
        )
    } catch (e: Exception) {
        CastledPushMessage.logger.error("Parsing xiaomi push payload failed!", e)
        return null
    }
}

fun MiPushMessage.isCastledPushMessage() =
    extra.containsKey(CastledNotificationFieldConsts.CASTLED_KEY)