package io.castled.android.notifications.workmanager.models

import io.castled.android.notifications.sessions.Sessions
import io.castled.android.notifications.store.CastledSharedStore

@kotlinx.serialization.Serializable
internal data class CastledInAppEvent(
    val teamId: String,
    val sourceContext: String,
    val btnLabel: String? = null,
    val actionType: String? = null,
    val actionUri: String? = null,
    val eventType: String,
    val tz: String,
    val ts: Long,
    val sessionId: String? = if (CastledSharedStore.configs.enableSessionTracking)
        Sessions.sessionId else null
)
