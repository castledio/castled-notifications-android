package io.castled.notifications.push.models

enum class PushTokenType(val platformName: String, val providerClassName: String) {

    FCM("fcm", "io.castled.notifications.push.FcmTokenProvider"),
    MI_PUSH("mipush", "io.castled.notifications.mipush.MiPushTokenProvider")
}