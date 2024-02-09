package io.castled.android.notifications.tracking.device

import io.castled.android.notifications.sessions.Sessions
import io.castled.android.notifications.store.CastledSharedStore
import io.castled.android.notifications.tracking.events.extensions.toJsonElement
import io.castled.android.notifications.workmanager.models.CastledDeviceInfoRequest
import kotlinx.serialization.json.JsonObject

internal object DeviceInfotUtils {

    fun getDeviceInfoRequest(
        deviceInfo: Map<String, Any>
    ): CastledDeviceInfoRequest {
        val event = CastledDeviceInfoRequest(
            type = "track",
            userId = CastledSharedStore.getUserId() ?: "",
            deviceInfo = JsonObject(deviceInfo.map { (key, value) ->
                key to value.toJsonElement()
            }.toMap()),
            sessionId = if (CastledSharedStore.configs.enableSessionTracking)
                Sessions.sessionId else null
        )
        return event
    }

}