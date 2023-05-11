package io.castled.notifications.inapp

import io.castled.notifications.commons.ClickActionParams
import io.castled.notifications.inapp.views.ButtonViewParams
import io.castled.notifications.store.models.Campaign
import io.castled.notifications.workmanager.models.CastledInAppEvent
import io.castled.notifications.workmanager.models.CastledInAppEventRequest
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
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