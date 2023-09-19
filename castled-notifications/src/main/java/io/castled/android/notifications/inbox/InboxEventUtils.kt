package io.castled.android.notifications.inbox

import io.castled.android.notifications.store.models.AppInbox
import io.castled.android.notifications.store.models.Campaign
import io.castled.android.notifications.workmanager.models.CastledInAppEvent
import io.castled.android.notifications.workmanager.models.CastledInAppEventRequest
import io.castled.android.notifications.workmanager.models.CastledInboxEvent
import io.castled.android.notifications.workmanager.models.CastledInboxEventRequest
import java.util.*

internal object InboxEventUtils {

    fun getInboxEventRequest(
        inbox: AppInbox, btnLabel: String, eventType: String
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

}