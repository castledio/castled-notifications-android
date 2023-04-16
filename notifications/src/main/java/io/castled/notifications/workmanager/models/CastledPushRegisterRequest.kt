package io.castled.notifications.workmanager.models

import kotlinx.serialization.Serializable

@Serializable
internal data class CastledPushRegisterRequest(val userId: String, val fcmToken: String?) :
    CastledNetworkRequest() {
    override val requestType: CastledNetworkRequestType = CastledNetworkRequestType.PUSH_REGISTER
}
