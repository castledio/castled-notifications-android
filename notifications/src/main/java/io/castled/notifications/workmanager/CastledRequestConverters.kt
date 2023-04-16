package io.castled.notifications.workmanager

import io.castled.notifications.push.models.NotificationEvent
import io.castled.notifications.workmanager.models.CastledPushEventRequest

internal object CastledRequestConverters {

    fun CastledPushEventRequest.toNotificationEvent(): NotificationEvent {
        val event = NotificationEvent()
        event.teamId = teamId
        event.sourceContext = sourceContext
        event.actionLabel = actionLabel
        event.actionType = actionType
        event.actionUri = actionUri
        event.eventType = eventType
        event.tz = tz
        event.ts = ts
        return event
    }

    fun NotificationEvent.toCastledPushEventRequest(): CastledPushEventRequest = CastledPushEventRequest(
        teamId = teamId,
        sourceContext = sourceContext,
        actionLabel = actionLabel,
        actionType = actionType,
        actionUri = actionUri,
        eventType = eventType,
        tz = tz,
        ts = ts
    )
}