package io.castled.android.notifications.inapp.models

enum class InAppMessageType {
    MODAL,
    FULL_SCREEN,
    BANNER
}

enum class InAppMessageTemplateType {
    DEFAULT,
    IMG_AND_BUTTONS,
    TEXT_AND_BUTTONS,
    IMG_ONLY,
    CUSTOM_HTML,
    OTHER
}