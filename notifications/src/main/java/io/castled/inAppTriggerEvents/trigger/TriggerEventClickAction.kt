package io.castled.inAppTriggerEvents.trigger

import io.castled.inAppTriggerEvents.eventConsts.TriggerEventConstants

internal interface TriggerEventClickAction {
    fun onTriggerEventAction(triggerEventConstants: TriggerEventConstants.Companion.EventClickType)
}