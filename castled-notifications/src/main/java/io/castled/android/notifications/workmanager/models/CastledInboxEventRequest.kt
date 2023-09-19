package io.castled.android.notifications.workmanager.models

import kotlinx.serialization.Serializable

@Serializable
internal data class CastledInboxEventRequest(
    val events: List<CastledInboxEvent>
) : CastledNetworkRequest() {
    override val requestType = CastledNetworkRequestType.INBOX_EVENT
}