package io.castled.android.notifications.push.extensions

import io.castled.android.notifications.push.NotificationActionContext
import io.castled.android.notifications.workmanager.models.CastledPushEvent
import io.castled.android.notifications.workmanager.models.CastledPushEventRequest
import java.util.*

internal fun NotificationActionContext.toCastledPushEventRequest(): CastledPushEventRequest {
    val event = CastledPushEvent(
        teamId = teamId,
        sourceContext = sourceContext,
        actionLabel = actionLabel,
        actionType = actionType,
        actionUri = actionUri,
        eventType = eventType,
        tz = TimeZone.getDefault().displayName,
        ts = System.currentTimeMillis() / 1000
    )
    return CastledPushEventRequest(listOf(event))
}