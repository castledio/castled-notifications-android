package io.castled.android.notifications.workmanager.models

import io.castled.android.notifications.sessions.Sessions
import io.castled.android.notifications.store.CastledSharedStore

@kotlinx.serialization.Serializable
internal data class CastledInboxEvent(
    val teamId: String,
    val eventType: String,
    val btnLabel: String? = null,
    val sourceContext: String,
    val tz: String,
    val ts: Long,
    val sessionId: String? = if (CastledSharedStore.configs.enableSessionTracking)
        Sessions.sessionId else null
)
