package io.castled.android.notifications.workmanager.models

import kotlinx.serialization.Serializable

@Serializable
internal data class CastledPushEventRequest(
   val events: List<CastledPushEvent>
) : CastledNetworkRequest() {
    override val requestType = CastledNetworkRequestType.PUSH_EVENT
}