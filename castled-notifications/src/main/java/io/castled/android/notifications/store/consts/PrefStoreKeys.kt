package io.castled.android.notifications.store.consts

internal class PrefStoreKeys {
    companion object {
        const val APP_ID = "appId"
        const val DEVICE_ID = "deviceId"
        const val USER_ID = "userId"
        const val CONFIGS = "configs"

        const val DEVICE_INFO = "deviceInfo"
        const val USER_TOKEN = "userToken"
        const val USER_ID_UNREGISTERED = "userIdUnregistered"
        const val FCM_TOKEN = "fcmToken"
        const val MI_TOKEN = "miToken"
        const val IS_PUSH_GRANTED = "isPushGranted"
        const val FCM_TOKEN_UNREGISTERED = "fcmTokenUnregistered"
        const val OS_VERSION = "osVersion"
        const val SDK_VERSION = "sdkVersion"
        const val RECENT_DISPLAYED_PUSH_IDS = "recentDisplayedPushIds"

        const val SESSION_ID = "sessionId"
        const val SESSION_DURATION = "sessionDuration"
        const val SESSION_START_TIME = "sessionStarTime"
        const val SESSION_END_TIME = "sessionEndTime"
        const val SESSION_IS_FIRST = "sessionIsFirst"

    }
}