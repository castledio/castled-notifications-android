package io.castled.android.notifications.workmanager.models

import io.castled.android.notifications.push.models.PushTokenInfo
import kotlinx.serialization.Serializable

@Serializable
internal data class CastledPushRegisterRequest(
    val userId: String,
    val tokens: List<PushTokenInfo>,
    val deviceId: String
) : CastledNetworkRequest() {
    override val requestType: CastledNetworkRequestType = CastledNetworkRequestType.PUSH_REGISTER
}
