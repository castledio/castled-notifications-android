package io.castled.android.notifications.inbox

import io.castled.android.notifications.inbox.model.CastledInboxItem
import io.castled.android.notifications.store.models.AppInbox
import io.castled.android.notifications.workmanager.models.CastledInboxEvent
import io.castled.android.notifications.workmanager.models.CastledInboxEventRequest
import java.util.TimeZone

internal object InboxEventUtils {

    fun getInboxEventRequest(
        inbox: CastledInboxItem, btnLabel: String, eventType: String
    ): CastledInboxEventRequest {
        val event = CastledInboxEvent(
            teamId = inbox.teamId.toString(),
            sourceContext = inbox.sourceContext,
            eventType = eventType,
            btnLabel = btnLabel,
            ts = System.currentTimeMillis() / 1000,
            tz = TimeZone.getDefault().displayName
        )
        return CastledInboxEventRequest(listOf(event))
    }

    fun getReadInboxEventRequest(
        inboxItems: Set<AppInbox>
    ): CastledInboxEventRequest {
        val batchedEvents = mutableListOf<CastledInboxEvent>()

        inboxItems.forEach { inbox ->
            batchedEvents.add(
                CastledInboxEvent(
                    teamId = inbox.teamId.toString(),
                    sourceContext = inbox.sourceContext,
                    eventType = "READ",
                    btnLabel = "",
                    ts = System.currentTimeMillis() / 1000,
                    tz = TimeZone.getDefault().displayName
                )
            )
        }
        return CastledInboxEventRequest(batchedEvents)
    }
}