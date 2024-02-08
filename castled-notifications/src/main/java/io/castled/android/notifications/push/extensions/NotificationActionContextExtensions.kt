package io.castled.android.notifications.push.extensions

import io.castled.android.notifications.push.models.CastledActionContext
import io.castled.android.notifications.push.models.CastledClickAction
import io.castled.android.notifications.push.models.NotificationActionContext
import io.castled.android.notifications.workmanager.models.CastledPushEvent
import io.castled.android.notifications.workmanager.models.CastledPushEventRequest
import java.util.*

internal fun NotificationActionContext.toCastledPushEventRequest(): CastledPushEventRequest {
    val event = CastledPushEvent(
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

internal fun NotificationActionContext.toCastledActionContext() =
    CastledActionContext(
        actionType = actionType?.let { CastledClickAction.valueOf(it) } ?: CastledClickAction.NONE,
        actionLabel = actionLabel,
        actionUri = actionUri,
        keyVals = keyVals,
    )
