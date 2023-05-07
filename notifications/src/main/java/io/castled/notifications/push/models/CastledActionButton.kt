package io.castled.notifications.push.models

import kotlinx.serialization.Serializable

@Serializable
data class CastledActionButton(
    val label: String,
    val url: String?,
    val clickAction: CastledClickAction,
    val keyVals: HashMap<String, String>?,
)
