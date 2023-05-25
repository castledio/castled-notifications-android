package io.castled.android.notifications.push.models

import kotlinx.serialization.Serializable

@Serializable
data class CastledPushMessageFrame(
    val title: String?,
    val subtitle: String?,
    val imageUrl: String? = null,
    val clickAction: CastledClickAction?,
    val clickActionUrl: String? = null,
    val keyVals: Map<String, String>? = null,
)