package io.castled.notifications.inapp

import io.castled.notifications.commons.ClickActionParams
import io.castled.notifications.inapp.views.ButtonViewParams
import io.castled.notifications.store.models.Campaign

internal interface InAppViewLifecycleListener {

    fun onDisplayed(inAppMessage: Campaign)

    fun onClicked(inAppViewBaseDecorator: InAppViewBaseDecorator, inAppMessage: Campaign, actionParams: ClickActionParams)

    fun onButtonClicked(inAppViewBaseDecorator: InAppViewBaseDecorator, inAppMessage: Campaign, actionParams: ClickActionParams?)

    fun onCloseButtonClicked(inAppViewBaseDecorator: InAppViewBaseDecorator, inAppMessage: Campaign)

    fun onClosed(inAppMessage: Campaign)
}