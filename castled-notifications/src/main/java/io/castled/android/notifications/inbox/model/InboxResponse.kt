package io.castled.android.notifications.inbox.model

import io.castled.android.notifications.commons.DateTimeUtils
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.util.Date

@Serializable
internal data class InboxResponse(
    val teamId: Long,
    val messageId: Long,
    val sourceContext: String,
    val read: Boolean,
    val startTs: Long,
    val expiryTs: Long,
    val trigger: JsonObject,
    val message: JsonObject
) {

    val aspectRatio: Number
        get() = (message["aspectRatio"])?.toString()?.toFloat() ?: 0.0
    val body: String
        get() = (message["body"])?.jsonPrimitive?.content ?: ""
    val date_added: Date
        get() {
            val date  = DateTimeUtils.getDateFromEpochTime(startTs)
            return DateTimeUtils.getDateFromEpochTime(startTs)
        }
    val messageType: InboxMessageType
        get() {
            val type = message["type"]?.jsonPrimitive?.content ?: "OTHER"
            return try {
                type.let { InboxMessageType.valueOf(it) }
            } catch (e: Exception) {
                InboxMessageType.valueOf("OTHER")
            }
        }
    val thumbnailUrl: String
        get() = (message["thumbnailUrl"]?.jsonPrimitive?.content
            ?: (message["contents"])?.let { it as? JsonArray }?.firstOrNull()
                ?.let { it as? JsonObject }?.get("thumbnailUrl")?.jsonPrimitive?.content)
            ?: (message["contents"]?.let { it as? JsonArray }?.firstOrNull()
                ?.let { it as? JsonObject }?.get("url")?.jsonPrimitive?.content) ?: ""

    val title: String
        get() = (message["title"])?.jsonPrimitive?.content ?: ""


}




