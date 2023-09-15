package io.castled.android.notifications.workmanager.trackevents

import io.castled.android.notifications.commons.DateTimeUtils
import io.castled.android.notifications.store.CastledSharedStore
import io.castled.android.notifications.workmanager.models.CastledTrackEvent
import io.castled.android.notifications.workmanager.models.CastledTrackEventRequest
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

internal object TrackEventUtils {

    fun getTrackEvent(
        eventName: String, properties: Map<String, Any>
    ): CastledTrackEventRequest {

        val event = CastledTrackEvent(
            type = "track",
            userId = CastledSharedStore.getUserId() ?: "",
            event = eventName,
            properties = JsonObject(properties.map { (key, value) ->
                key to JsonPrimitive(value.toString())
            }.toMap()),
            timestamp = DateTimeUtils.getCurrentTimeFormatted()
        )
        return CastledTrackEventRequest(listOf(event))
    }

}