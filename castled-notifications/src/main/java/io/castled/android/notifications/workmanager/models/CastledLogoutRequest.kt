package io.castled.android.notifications.workmanager.models

import io.castled.android.notifications.push.models.PushTokenInfo
import kotlinx.serialization.Serializable

@Serializable
internal data class CastledLogoutRequest(
    val userId: String,
    val tokens: List<PushTokenInfo>,
    val sessionId: String?
) : CastledNetworkRequest() {
    override val requestType: CastledNetworkRequestType = CastledNetworkRequestType.LOGOUT
}
