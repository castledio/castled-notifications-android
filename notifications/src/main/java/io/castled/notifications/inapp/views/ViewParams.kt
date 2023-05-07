package io.castled.notifications.inapp.views

import io.castled.notifications.push.models.CastledClickAction

data class HeaderViewParams(
    val header: String,
    val fontColor: String,
    val fontSize: Float,
    val backgroundColor: String
)

data class MessageViewParams(
    val message: String,
    val fontColor: String,
    val fontSize: Float,
    val backgroundColor: String
)

data class ButtonViewParams(
    val buttonText: String,
    val fontColor: String,
    val buttonColor: String,
    val borderColor: String,
    val action: CastledClickAction,
    val uri: String?,
    val keyVals: Map<String, String>?
)

data class ImageViewParams(val imageUrl: String)