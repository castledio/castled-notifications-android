package io.castled.notifications.commons

import io.castled.notifications.push.models.CastledClickAction

data class ClickActionParams(
    val action: CastledClickAction,
    val actionLabel: String?,
    val uri: String?,
    val keyVals: Map<String, String>?
)
