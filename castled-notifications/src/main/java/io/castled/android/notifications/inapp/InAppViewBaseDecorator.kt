package io.castled.android.notifications.inapp

import io.castled.android.notifications.push.models.CastledClickAction

interface InAppViewBaseDecorator {

    fun show(withApiCall: Boolean)

    fun close(action: CastledClickAction)
}