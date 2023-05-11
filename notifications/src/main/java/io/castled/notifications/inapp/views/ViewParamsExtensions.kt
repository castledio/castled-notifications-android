package io.castled.notifications.inapp.views

import io.castled.notifications.commons.ClickActionParams

fun ButtonViewParams.toActionParams() = ClickActionParams(
    action = action,
    actionLabel = buttonText,
    uri = uri,
    keyVals = keyVals
)