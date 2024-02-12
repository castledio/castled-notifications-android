package io.castled.android.notifications.workmanager.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
internal data class CastledUserTrackingEventRequest(
    val userId: String,
    val traits: JsonObject,
    val timestamp: String,
    val sessionId: String? = null,
) : CastledNetworkRequest() {
    override val requestType = CastledNetworkRequestType.USER_TRACKING
}