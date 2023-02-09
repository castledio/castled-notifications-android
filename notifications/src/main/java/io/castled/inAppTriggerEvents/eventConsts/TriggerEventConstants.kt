package io.castled.inAppTriggerEvents.eventConsts

internal class TriggerEventConstants {
    companion object {
        const val notificationUrl = "https://test.castled.io/"

        enum class TriggerEventType {
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