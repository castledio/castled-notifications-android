package io.castled.android.notifications.inapp

import io.castled.android.notifications.commons.ClickActionParams
import io.castled.android.notifications.store.models.Campaign
import io.castled.android.notifications.workmanager.models.CastledInAppEvent
import io.castled.android.notifications.workmanager.models.CastledInAppEventRequest
import java.util.*

internal object InAppEventUtils {

    fun getViewedEvent(campaign: Campaign): CastledInAppEventRequest {
        val event = CastledInAppEvent(
            teamId = campaign.teamId.toString(),
            sourceContext = campaign.sourceContext,
            eventType = "VIEWED",
            ts = System.currentTimeMillis() / 1000,
            tz = TimeZone.getDefault().displayName
        )
        return CastledInAppEventRequest(listOf(event))
    }

    fun getDismissedEvent(campaign: Campaign): CastledInAppEventRequest {
        val event = CastledInAppEvent(
            teamId = campaign.teamId.toString(),
            sourceContext = campaign.sourceContext,
            eventType = "DISCARDED",
            ts = System.currentTimeMillis() / 1000,
            tz = TimeZone.getDefault().displayName
        )
        return CastledInAppEventRequest(listOf(event))
    }

    fun getClickedEvent(
        campaign: Campaign, actionParams: ClickActionParams
    ): CastledInAppEventRequest {
        val event = CastledInAppEvent(
            teamId = campaign.teamId.toString(),
            sourceContext = campaign.sourceContext,
            eventType = "CLICKED",
            btnLabel = actionParams.actionLabel,
            actionType = actionParams.action.toString(),
            actionUri = actionParams.uri,
            ts = System.currentTimeMillis() / 1000,
            tz = TimeZone.getDefault().displayName
        )
        return CastledInAppEventRequest(listOf(event))
    }

}