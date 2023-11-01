package io.castled.android.notifications.workmanager.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
internal data class CastledDeviceInfoRequest(
    val userId: String,
    val deviceInfo: JsonObject,
    @SerialName("trackReqType")
    val type: String
) : CastledNetworkRequest() {
    override val requestType = CastledNetworkRequestType.DEVICE_INFO
}