package io.castled.notifications.push.models

import io.castled.notifications.logger.CastledLogger
import io.castled.notifications.logger.LogTags
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

@Serializable
data class CastledPushMessage(
    val notificationId: Int,
    val sourceContext: String,
    val teamId: Long,
    val title: String?,
    val body: String?,
    val summary: String?,
    val imageUrl: String?,
    val sound: String?,
    val priority: CastledPushPriority?,
    val clickAction: CastledClickAction?,
    val clickActionUri: String?,
    val keyVals: Map<String, String>?,
    val channelId: String?,
    val channelName: String?,
    val channelDescription: String?,
    val smallIconResourceId: String?,
    val largeIconUri: String?,
    val castledActionButtons: List<CastledActionButton>?,
    val ttl: Long?
) {

    companion object {

        val logger = CastledLogger.getInstance(LogTags.PUSH)

        fun getPushMessageFromMap(map: Map<String, Any?>): CastledPushMessage? {
            try {
                return CastledPushMessage(
                    notificationId = (map["nId"] as String).toInt(),
                    sourceContext = map["srcCtx"] as String,
                    teamId = (map["tId"] as String).toLong(),
                    title = map["title"] as? String,
                    body = map["body"] as? String,
                    summary = map["summary"] as? String,
                    imageUrl = map["imageUrl"] as? String,
                    sound = map["sound"] as? String,
                    priority = (map["priority"] as? String)?.let { CastledPushPriority.valueOf(it) },
                    clickAction = (map["clickAction"] as? String)?.let { CastledClickAction.valueOf(it) },
                    clickActionUri = map["clickActionUri"] as? String,
                    keyVals = (map["keyVals"] as? String)?.let { Json.decodeFromString(it) },
                    channelId = map["channelId"] as? String,
                    channelName = map["channelName"] as? String,
                    channelDescription = map["channelDescription"] as? String,
                    smallIconResourceId = map["smallIconResourceId"] as? String,
                    largeIconUri = map["largeIconUri"] as? String,
                    castledActionButtons = (map["actionButtons"] as? String)?.let { Json.decodeFromString(it) },
                    ttl = (map["ttl"] as? String)?.toLong()
                )
            } catch (e: Exception) {
                logger.error("Parsing fcm push payload failed!", e)
                return null
            }
        }
    }


}