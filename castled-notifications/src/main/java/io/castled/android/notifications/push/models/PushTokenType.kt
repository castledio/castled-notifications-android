package io.castled.android.notifications.push.models

enum class PushTokenType(val platformName: String, val providerClassName: String) {

    FCM("fcm", "io.castled.android.notifications.push.FcmTokenProvider"),
    MI_PUSH("mipush", "io.castled.android.notifications.mipush.MiPushTokenProvider")
}