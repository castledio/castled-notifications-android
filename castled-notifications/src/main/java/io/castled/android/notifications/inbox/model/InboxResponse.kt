package io.castled.android.notifications.inbox.model
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

@Serializable
internal data class InboxResponse(
    val teamId: Long,
    val messageId: Long,
    val sourceContext: String,
    val read: Boolean,
    val startTs: Long,
    val expiryTs: Long,
    val trigger: JsonObject,
    val message: JsonObject)
{

    val thumbnailUrl: String
        get() = (message["thumbnailUrl"])?.toString() ?:
        (message["contents"]
        ?.let { it as? JsonArray }
        ?.firstOrNull()
        ?.let { it as? JsonObject }
        ?.get("thumbnailUrl")
        ?.takeIf { it is JsonPrimitive }
        ?.toString()) ?:
    (message["contents"]
        ?.let { it as? JsonArray }
        ?.firstOrNull()
        ?.let { it as? JsonObject }
        ?.get("url")
        ?.takeIf { it is JsonPrimitive }
        ?.toString()) ?: ""

    val aspectRatio: Number
        get() = (message["aspectRatio"])?.toString()?.toFloat() ?: 0.0
    val messageType: InboxMessageType
        get() = (InboxMessageType.valueOf(message["type"]?.toString() ?: "OTHER"))

}




