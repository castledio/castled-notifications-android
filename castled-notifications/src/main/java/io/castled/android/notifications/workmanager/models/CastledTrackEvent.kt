package io.castled.android.notifications.workmanager.models

import kotlinx.serialization.json.JsonObject

@kotlinx.serialization.Serializable
internal data class CastledTrackEvent(
    val type: String,
    val userId: String,
    val event: String,
    val properties: JsonObject? = null,
    val timestamp: String
)
