package io.castled.android.notifications.commons

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive

fun JsonObject.toMapString(): Map<String, String>? {
    this.let {
        return this.entries.associate { (key, jsonElement) ->
            key to jsonElement.jsonPrimitive.content
        }
    }
}
