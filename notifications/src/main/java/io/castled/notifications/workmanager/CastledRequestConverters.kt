package io.castled.notifications.workmanager

import io.castled.notifications.push.models.NotificationActionContext
import io.castled.notifications.workmanager.models.CastledPushEventRequest
import java.util.TimeZone

internal object CastledRequestConverters {

    fun NotificationActionContext.toCastledPushEventRequest(): CastledPushEventRequest =
        CastledPushEventRequest(
            teamId = teamId,
            sourceContext = sourceContext,
            actionLabel = actionLabel,
            actionType = actionType,
            actionUri = actionUri,
            eventType = eventType,
            tz = TimeZone.getDefault().displayName,
            ts = System.currentTimeMillis() / 1000
        )
}