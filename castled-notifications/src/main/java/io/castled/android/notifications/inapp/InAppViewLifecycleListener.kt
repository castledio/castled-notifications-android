package io.castled.android.notifications.inapp

import android.content.Context
import io.castled.android.notifications.commons.ClickActionParams
import io.castled.android.notifications.inapp.views.ButtonViewParams
import io.castled.android.notifications.store.models.Campaign

internal interface InAppViewLifecycleListener {

    fun onDisplayed(inAppMessage: Campaign)

    fun onClicked(
        context: Context,
        inAppViewBaseDecorator: InAppViewBaseDecorator,
        inAppMessage: Campaign,
        actionParams: ClickActionParams
    )

    fun onButtonClicked(
        context: Context,
        inAppViewBaseDecorator: InAppViewBaseDecorator,
        inAppMessage: Campaign,
        actionParams: ClickActionParams?
    )

    fun onCloseButtonClicked(
        context: Context,
        inAppViewBaseDecorator: InAppViewBaseDecorator,
        inAppMessage: Campaign
    )

    fun onClosed(inAppMessage: Campaign)
}