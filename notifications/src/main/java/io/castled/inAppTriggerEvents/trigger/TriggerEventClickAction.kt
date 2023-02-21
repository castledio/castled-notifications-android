package io.castled.inAppTriggerEvents.trigger

import com.google.gson.JsonObject
import io.castled.inAppTriggerEvents.eventConsts.TriggerEventConstants

internal interface TriggerEventClickAction {
    fun onTriggerEventAction(triggerEventConstants: TriggerEventConstants.Companion.EventClickType)
}