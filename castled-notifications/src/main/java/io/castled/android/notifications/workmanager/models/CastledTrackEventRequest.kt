package io.castled.android.notifications.workmanager.models

import kotlinx.serialization.Serializable

@Serializable
internal data class CastledTrackEventRequest(
    val event: CastledTrackEvent

) : CastledNetworkRequest() {
    override val requestType = CastledNetworkRequestType.TRACK_EVENT
}