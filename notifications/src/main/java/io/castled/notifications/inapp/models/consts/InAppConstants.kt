package io.castled.notifications.inapp.models.consts

internal class InAppConstants {

    companion object {
        enum class InAppTemplateType {
            MODAL,
            FULL_SCREEN,
            SLIDE_UP,
            NONE
        }

        enum class EventClickType {
            PRIMARY_BUTTON,
            SECONDARY_BUTTON,
            CLOSE_EVENT,
            IMAGE_CLICK
        }
    }
}