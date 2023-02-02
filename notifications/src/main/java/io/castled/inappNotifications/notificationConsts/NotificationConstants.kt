package io.castled.inappNotifications.notificationConsts

class NotificationConstants {
    companion object {
        val notificationUrl = "https://test.castled.io/"

        enum class NotificationType {
            MODAL,
            FULL_SCREEN,
            SLIDE_UP,
            NONE
        }
    }
}