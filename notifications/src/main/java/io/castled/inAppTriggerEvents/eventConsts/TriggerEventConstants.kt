package io.castled.inAppTriggerEvents.eventConsts

internal class TriggerEventConstants {
    companion object {
        val notificationUrl = "https://test.castled.io/"

        enum class TriggerEventType {
            MODAL,
            FULL_SCREEN,
            SLIDE_UP,
            NONE
        }
    }
}