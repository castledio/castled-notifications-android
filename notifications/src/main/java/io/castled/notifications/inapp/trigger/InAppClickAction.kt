package io.castled.notifications.inapp.trigger

import io.castled.notifications.inapp.models.consts.InAppConstants

internal interface InAppClickAction {
    fun onTrigger(clickType: InAppConstants.Companion.EventClickType)
}