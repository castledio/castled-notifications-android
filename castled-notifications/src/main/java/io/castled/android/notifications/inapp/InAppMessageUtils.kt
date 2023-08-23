package io.castled.android.notifications.inapp

import io.castled.android.notifications.inapp.models.InAppMessageTemplateType
import io.castled.android.notifications.inapp.models.InAppMessageType
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject

internal object InAppMessageUtils {

    fun getMessageType(message: JsonObject): InAppMessageType {
        return (message["type"] as JsonPrimitive)
            .let { InAppMessageType.valueOf(it.content) }
    }

    fun getMessageTemplateType(type: String): InAppMessageTemplateType {
        return try {
            type.let { InAppMessageTemplateType.valueOf(it) }
        } catch (e: Exception) {
            type.let { InAppMessageTemplateType.valueOf("OTHER") }
        }
    }

    fun getMessageBody(message: JsonObject): JsonObject {
        return when (getMessageType(message)) {
            InAppMessageType.MODAL -> message["modal"]?.jsonObject!!
            InAppMessageType.FULL_SCREEN -> message["fs"]?.jsonObject!!
            InAppMessageType.BANNER -> message["banner"]?.jsonObject!!
        }
    }

}