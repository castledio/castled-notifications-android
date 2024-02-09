package io.castled.android.notifications.workmanager.models

import io.castled.android.notifications.sessions.events.CastledSessionEvent
import kotlinx.serialization.Serializable

@Serializable
internal data class CastledSessionRequest(
    val events: List<CastledSessionEvent>

) : CastledNetworkRequest() {
    override val requestType = CastledNetworkRequestType.SESSION
}