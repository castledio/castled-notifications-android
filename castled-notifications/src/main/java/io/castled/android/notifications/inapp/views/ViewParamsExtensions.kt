package io.castled.android.notifications.inapp.views

import io.castled.android.notifications.commons.ClickActionParams

fun ButtonViewParams.toActionParams() = ClickActionParams(
    action = action,
    actionLabel = buttonText,
    uri = uri,
    keyVals = keyVals
)