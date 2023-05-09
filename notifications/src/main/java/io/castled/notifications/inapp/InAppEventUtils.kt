package io.castled.notifications.inapp

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

    fun getClickedEvent(campaign: Campaign): CastledInAppEventRequest {
        val message: JsonObject = campaign.message
        val msgBody: JsonObject = message["modal"] as JsonObject?
            ?: message["fs"] as JsonObject?
            ?: message["slideUp"] as JsonObject

        val actionType = (msgBody["defaultClickAction"] as JsonPrimitive?)?.content
        val actionUri = (msgBody["url"] as JsonPrimitive?)?.content

        val event = CastledInAppEvent(
            teamId = campaign.teamId.toString(),
            sourceContext = campaign.sourceContext,
            eventType = "CLICKED",
            actionType = actionType,
            actionUri = actionUri,
            ts = System.currentTimeMillis() / 1000,
            tz = TimeZone.getDefault().displayName
        )
        return CastledInAppEventRequest(listOf(event))
    }

    fun getButtonClickedEvent(
        campaign: Campaign, btnParams: ButtonViewParams
    ): CastledInAppEventRequest {
        val event = CastledInAppEvent(
            teamId = campaign.teamId.toString(),
            sourceContext = campaign.sourceContext,
            eventType = "CLICKED",
            btnLabel = btnParams.buttonText,
            actionType = btnParams.action.toString(),
            actionUri = btnParams.uri,
            ts = System.currentTimeMillis() / 1000,
            tz = TimeZone.getDefault().displayName
        )
        return CastledInAppEventRequest(listOf(event))
    }

}