package io.castled.android.notifications.push.extensions

import com.google.firebase.messaging.RemoteMessage
import io.castled.android.notifications.push.models.CastledClickAction
import io.castled.android.notifications.push.models.CastledPushMessage
import io.castled.android.notifications.push.models.CastledPushPriority
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

fun RemoteMessage.toCastledPushMessage() : CastledPushMessage? {
    try {
        return CastledPushMessage(
            notificationId = (data["nId"] as String).toInt(),
            sourceContext = data["srcCtx"] as String,
            teamId = (data["tId"] as String).toLong(),
            title = data["title"],
            body = data["body"],
            summary = data["summary"],
            imageUrl = data["imageUrl"],
            sound = data["sound"],
            priority = data["priority"]?.let { CastledPushPriority.valueOf(it) },
            clickAction = data["clickAction"]?.let { CastledClickAction.valueOf(it) },
            clickActionUri = data["clickActionUri"],
            keyVals = data["keyVals"]?.let { Json.decodeFromString(it) },
            channelId = data["channelId"],
            channelName = data["channelName"],
            channelDescription = data["channelDescription"],
            smallIconResourceId = data["smallIconResourceId"],
            largeIconUri = data["largeIconUri"],
            castledActionButtons = data["actionButtons"]?.let { Json.decodeFromString(it) },
            ttl = data["ttl"]?.toLong()
        )
    } catch (e: Exception) {
        CastledPushMessage.logger.error("Parsing fcm push payload failed!", e)
        return null
    }
}