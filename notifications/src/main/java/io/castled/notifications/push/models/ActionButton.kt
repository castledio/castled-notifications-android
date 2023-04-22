package io.castled.notifications.push.models

import kotlinx.serialization.Serializable

@Serializable
internal data class ActionButton(
    val label: String,
    val url: String?,
    val clickAction: ClickAction,
    val keyVals: HashMap<String, String>?,
)
