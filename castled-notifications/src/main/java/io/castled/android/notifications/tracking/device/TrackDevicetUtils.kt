package io.castled.android.notifications.tracking.device

import io.castled.android.notifications.store.CastledSharedStore
import io.castled.android.notifications.tracking.events.extensions.toJsonElement
import io.castled.android.notifications.workmanager.models.CastledDeviceTrackingRequest
import kotlinx.serialization.json.JsonObject

internal object TrackDevicetUtils {

    fun getDeviceTrackRequest(
        deviceInfo: Map<String, Any>
    ): CastledDeviceTrackingRequest {
        val event = CastledDeviceTrackingRequest(
            type = "track",
            userId = CastledSharedStore.getUserId() ?: "",
            deviceInfo = JsonObject(deviceInfo.map { (key, value) ->
                key to value.toJsonElement()
            }.toMap())
        )
        return event
    }

}