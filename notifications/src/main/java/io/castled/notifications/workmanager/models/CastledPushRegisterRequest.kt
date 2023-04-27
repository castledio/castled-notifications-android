package io.castled.notifications.workmanager.models

import io.castled.notifications.push.models.PushTokenInfo
import kotlinx.serialization.Serializable

@Serializable
internal data class CastledPushRegisterRequest(val userId: String, val tokens: List<PushTokenInfo>) :
    CastledNetworkRequest() {
    override val requestType: CastledNetworkRequestType = CastledNetworkRequestType.PUSH_REGISTER
}
