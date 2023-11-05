package io.castled.android.notifications.push.extensions

import com.google.firebase.messaging.RemoteMessage
import io.castled.android.notifications.logger.CastledLogger
import io.castled.android.notifications.logger.LogTags
import io.castled.android.notifications.push.models.CastledPushMessage
import io.castled.android.notifications.push.models.CastledPushPriority
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

private val json = Json { ignoreUnknownKeys = true }

fun RemoteMessage.toCastledPushMessage(): CastledPushMessage? {
    try {
        return CastledPushMessage(
            notificationId = (data["nId"] as String).toInt(),
            sourceContext = data["srcCtx"] as String,
            teamId = (data["tId"] as String).toLong(),
            title = data["title"],
            body = data["body"],
            summary = data["summary"],
            sound = data["sound"],
            priority = data["priority"]?.let { CastledPushPriority.valueOf(it) },
            channelId = data["channelId"],
            channelName = data["channelName"],
            channelDescription = data["channelDescription"],
            smallIconResourceId = data["smallIconResourceId"],
            largeIconUri = data["largeIconUri"],
            pushMessageFrames =  json.decodeFromString(data["msgFrames"]!!),
            actionButtons = data["actionButtons"]?.let { Json.decodeFromString(it) },
            ttl = data["ttl"]?.toLong()
        )
    } catch (e: Exception) {
        CastledLogger.getInstance(LogTags.PUSH).debug("Parsing fcm push payload failed! error:${e.message}")
        return null
    }
}