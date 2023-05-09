package io.castled.notifications.inapp

import io.castled.notifications.inapp.views.ButtonViewParams
import io.castled.notifications.store.models.Campaign

internal interface InAppViewLifecycleListener {

    fun onDisplayed(inAppMessage: Campaign)

    fun onClicked(inAppMessage: Campaign)

    fun onButtonClicked(inAppViewBaseDecorator: InAppViewBaseDecorator, inAppMessage: Campaign, btnParams: ButtonViewParams?)

    fun onCloseButtonClicked(inAppViewBaseDecorator: InAppViewBaseDecorator, inAppMessage: Campaign)

    fun onClosed(inAppMessage: Campaign)
}