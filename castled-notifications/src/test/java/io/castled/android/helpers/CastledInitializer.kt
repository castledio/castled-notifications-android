package io.castled.android.helpers

import android.app.Application
import io.castled.android.notifications.CastledConfigs
import io.castled.android.notifications.CastledNotifications

object CastledInitializer {
    var isInitialized = false
    fun initializeCastled(
        application: Application
    ) {
        if (isInitialized) {
            return
        }
        isInitialized = true
        CastledNotifications.initialize(
            application,
            CastledConfigs.Builder()
                .appId("718c38e2e359d94367a2e0d35e1fd4df")
                .location(CastledConfigs.CastledLocation.US)
                .enableAppInbox(true)
                .enableInApp(true)
                .enablePush(false)
                .enablePushBoost(false)
                .enableSessionTracking(false)
                .skipUrlHandling(false)
                .enableTracking(false)
                .inAppFetchIntervalSec(60)
                .sessionTimeOutSec(900L)
                .build()
        )

        // User-id needs to set after login flow of your app is complete
        CastledNotifications.setUserId(application, "antony@castled.io")
    }
}