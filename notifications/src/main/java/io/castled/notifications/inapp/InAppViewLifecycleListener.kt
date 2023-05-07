package io.castled.notifications.inapp

import io.castled.notifications.inapp.views.ButtonViewParams
import io.castled.notifications.store.models.Campaign

internal interface InAppViewLifecycleListener {

    fun onDisplayed(inAppMessage: Campaign)

    fun onClicked(inAppMessage: Campaign)

    fun onButtonClicked(inAppViewDecorator: InAppViewDecorator, inAppMessage: Campaign, btnParams: ButtonViewParams?)

    fun onCloseButtonClicked(inAppViewDecorator: InAppViewDecorator, inAppMessage: Campaign)

    fun onDismissed(inAppMessage: Campaign)
}