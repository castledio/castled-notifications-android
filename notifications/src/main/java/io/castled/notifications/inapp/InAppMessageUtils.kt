package io.castled.notifications.inapp

import io.castled.notifications.inapp.models.InAppMessageType
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject

internal object InAppMessageUtils {

    fun getMessageType(message: JsonObject): InAppMessageType {
        return (message["type"] as JsonPrimitive)
            .let { InAppMessageType.valueOf(it.content) }
    }

    fun getMessageBody(message: JsonObject): JsonObject {
        return when (getMessageType(message)) {
            InAppMessageType.MODAL -> message["modal"]?.jsonObject!!
            InAppMessageType.FULL_SCREEN -> message["fs"]?.jsonObject!!
            InAppMessageType.BANNER -> message["banner"]?.jsonObject!!
        }
    }

}