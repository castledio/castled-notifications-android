package io.castled.notifications.push.models

import io.castled.notifications.logger.CastledLogger
import io.castled.notifications.logger.LogTags
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

@Serializable
internal data class CastledPushPayload(
    val notificationId: Int,
    val sourceContext: String,
    val teamId: Long,
    val title: String?,
    val body: String?,
    val summary: String?,
    val imageUrl: String?,
    val sound: String?,
    val tag: String?,
    val priority: PushPriority?,
    val clickAction: ClickAction?,
    val clickActionUrl: String?,
    val keyVals: Map<String, String>?,
    val channelId: String?,
    val channelName: String?,
    val channelDescription: String?,
    val smallIconResourceId: String?,
    val largeIconUrl: String?,
    val actionButtons: List<ActionButton>?,
    val ttl: Long?
) {

    companion object {

        val logger = CastledLogger.getInstance(LogTags.PUSH)

        fun createPushPayloadFromMap(map: Map<String, Any?>): CastledPushPayload? {
            try {
                return CastledPushPayload(
                    notificationId = (map["notificationId"] as String).toInt(),
                    sourceContext = map["sourceContext"] as String,
                    teamId = (map["teamId"] as String).toLong(),
                    title = map["title"] as? String,
                    body = map["body"] as? String,
                    summary = map["summary"] as? String,
                    imageUrl = map["imageUrl"] as? String,
                    sound = map["sound"] as? String,
                    tag = map["tag"] as? String,
                    priority = (map["priority"] as? String)?.let { PushPriority.valueOf(it) },
                    clickAction = (map["clickAction"] as? String)?.let { ClickAction.valueOf(it) },
                    clickActionUrl = map["clickActionUrl"] as? String,
                    keyVals = (map["keyVals"] as? String)?.let { Json.decodeFromString(it) },
                    channelId = map["channelId"] as? String,
                    channelName = map["channelName"] as? String,
                    channelDescription = map["channelDescription"] as? String,
                    smallIconResourceId = map["smallIconResourceId"] as? String,
                    largeIconUrl = map["largeIconUrl"] as? String,
                    actionButtons = (map["actionButtons"] as? String)?.let { Json.decodeFromString(it) },
                    ttl = (map["ttl"] as? String)?.toLong()
                )
            } catch (e: Exception) {
                logger.error("Parsing push payload failed!", e)
                return null
            }
        }
    }


}