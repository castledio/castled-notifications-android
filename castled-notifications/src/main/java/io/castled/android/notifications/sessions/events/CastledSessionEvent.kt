package io.castled.android.notifications.sessions.events

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
internal data class CastledSessionEvent(
    val firstSession: Boolean? = false,
    val sessionId: String,
    val sessionEventType: String,
    val userId: String,
    val timestamp: String? = null,
    val duration: Long? = null,
    val properties: JsonObject? = null
)
