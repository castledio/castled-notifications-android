package io.castled.notifications.workmanager.models

import kotlinx.serialization.Polymorphic
import kotlinx.serialization.Serializable

@Serializable
@Polymorphic
internal sealed class CastledNetworkRequest {
    abstract val requestType : CastledNetworkRequestType
}
