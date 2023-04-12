package io.castled.notifications.workmanager.models

import kotlinx.serialization.Serializable

@Serializable
internal data class CastledInAppEventRequest(
    val teamId: String,
    val sourceContext: String,
    val btnLabel: String? = null,
    val actionType: String? = null,
    val actionUri: String? = null,
    val eventType: String,
    val tz: String,
    val ts: Long
) : CastledNetworkRequest() {
    override val requestType = CastledNetworkRequestType.IN_APP_EVENT
}