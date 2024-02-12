package io.castled.android.notifications.tracking.events

import io.castled.android.notifications.commons.DateTimeUtils
import io.castled.android.notifications.sessions.Sessions
import io.castled.android.notifications.store.CastledSharedStore
import io.castled.android.notifications.tracking.events.extensions.toJsonElement
import io.castled.android.notifications.workmanager.models.CastledTrackEvent
import io.castled.android.notifications.workmanager.models.CastledTrackEventRequest
import io.castled.android.notifications.workmanager.models.CastledUserTrackingEventRequest
import kotlinx.serialization.json.JsonObject

internal object TrackEventUtils {

    fun getTrackEvent(
        eventName: String, properties: Map<String, Any?>
    ): CastledTrackEventRequest {

        val event = CastledTrackEvent(
            type = "track",
            userId = CastledSharedStore.getUserId() ?: "",
            event = eventName,
            properties = JsonObject(properties.map { (key, value) ->
                key to value.toJsonElement()
            }.toMap()),
            timestamp = DateTimeUtils.getCurrentTimeFormatted(),
            sessionId = Sessions.sessionId
        )
        return CastledTrackEventRequest(listOf(event))
    }

    fun getUserEvent(
        traits: Map<String, Any?>
    ): CastledUserTrackingEventRequest {

        val event = CastledUserTrackingEventRequest(
            userId = CastledSharedStore.getUserId() ?: "",
            traits = JsonObject(traits.map { (key, value) ->
                key to value.toJsonElement()
            }.toMap()),
            timestamp = DateTimeUtils.getCurrentTimeFormatted(),
            sessionId = Sessions.sessionId
        )
        return event
    }

}