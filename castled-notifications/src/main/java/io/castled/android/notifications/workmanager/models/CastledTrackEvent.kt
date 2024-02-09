package io.castled.android.notifications.workmanager.models

import io.castled.android.notifications.sessions.Sessions
import io.castled.android.notifications.store.CastledSharedStore
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
internal data class CastledTrackEvent(
    val type: String,
    val userId: String,
    val event: String,
    val properties: JsonObject? = null,
    val timestamp: String,
    val sessionId: String? = if (CastledSharedStore.configs.enableSessionTracking)
        Sessions.sessionId else null
)
