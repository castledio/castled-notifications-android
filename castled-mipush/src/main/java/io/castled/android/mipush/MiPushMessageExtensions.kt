package io.castled.android.mipush

import com.xiaomi.mipush.sdk.MiPushMessage
import io.castled.android.notifications.logger.CastledLogger
import io.castled.android.notifications.logger.LogTags
import io.castled.android.notifications.push.models.CastledNotificationFieldConsts
import io.castled.android.notifications.push.models.CastledPushMessage
import io.castled.android.notifications.push.models.CastledPushPriority
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

private val json = Json { ignoreUnknownKeys = true }

fun MiPushMessage.toCastledPushMessage(): CastledPushMessage? {
    try {
        return CastledPushMessage(
            notificationId = notifyId,
            sourceContext = extra["srcCtx"]!!,
            teamId = extra["tId"]?.toLong()!!,
            title = title,
            body = content,
            summary = description,
            pushMessageFrames = json.decodeFromString(extra["msgFrames"]!!),
            sound = extra["sound"],
            priority = extra["priority"]?.let { CastledPushPriority.valueOf(it) },
            channelId = extra["channelId"],
            channelName = extra["channelName"],
            channelDescription = extra["channelDescription"],
            smallIconResourceId = extra["smallIconResourceId"],
            largeIconUri = extra["largeIconUri"],
            actionButtons = extra["actionButtons"]?.let { Json.decodeFromString(it) },
            ttl = extra["ttl"]?.toLong()
        )
    } catch (e: Exception) {
        CastledLogger.getInstance(LogTags.PUSH).error("Parsing xiaomi push payload failed!", e)
        return null
    }
}

fun MiPushMessage.isCastledPushMessage() =
    extra.containsKey(CastledNotificationFieldConsts.CASTLED_KEY)