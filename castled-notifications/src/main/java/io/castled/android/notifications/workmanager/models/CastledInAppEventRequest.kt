package io.castled.android.notifications.workmanager.models

import kotlinx.serialization.Serializable

@Serializable
internal data class CastledInAppEventRequest(
    val events: List<CastledInAppEvent>
) : CastledNetworkRequest() {
    override val requestType = CastledNetworkRequestType.IN_APP_EVENT
}