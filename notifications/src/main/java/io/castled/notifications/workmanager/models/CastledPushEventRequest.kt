package io.castled.notifications.workmanager.models

import kotlinx.serialization.Serializable

@Serializable
internal data class CastledPushEventRequest(
    val teamId: Long,
    val sourceContext: String,
    val actionLabel: String?,
    val actionType: String?,
    val actionUri: String?,
    val eventType: String,
    val tz: String,
    val ts: Long
) : CastledNetworkRequest() {
    override val requestType = CastledNetworkRequestType.PUSH_EVENT
}