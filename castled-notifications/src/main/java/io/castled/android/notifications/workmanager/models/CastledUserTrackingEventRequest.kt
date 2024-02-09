package io.castled.android.notifications.workmanager.models

import io.castled.android.notifications.sessions.Sessions
import io.castled.android.notifications.store.CastledSharedStore
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
internal data class CastledUserTrackingEventRequest(
    val userId: String,
    val traits: JsonObject,
    val timestamp: String,
    val sessionId: String? = if (CastledSharedStore.configs.enableSessionTracking)
        Sessions.sessionId else null
) : CastledNetworkRequest() {
    override val requestType = CastledNetworkRequestType.USER_TRACKING
}