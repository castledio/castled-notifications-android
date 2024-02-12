package io.castled.android.notifications.workmanager.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
internal data class CastledTrackEvent(
    val type: String,
    val userId: String,
    val event: String,
    val properties: JsonObject? = null,
    val timestamp: String,
    var sessionId: String? = null
)
