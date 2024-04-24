package io.castled.android.notifications.tracking.events.extensions

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive

fun Any?.toJsonElement(): JsonElement {
    return when (this) {
        is Number -> JsonPrimitive(this)
        is Boolean -> JsonPrimitive(this)
        null -> JsonPrimitive(null as String?)
        else -> JsonPrimitive(this.toString())
    }
}