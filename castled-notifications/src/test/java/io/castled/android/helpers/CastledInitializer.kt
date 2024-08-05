package io.castled.android.helpers

import android.app.Application
import io.castled.android.notifications.CastledConfigs
import io.castled.android.notifications.CastledNotifications

object CastledInitializer {
    fun initializeCastled(
        application: Application,
        enableAppInbox: Boolean = false,
        enableInApp: Boolean = false,
        enableTracking: Boolean = false,
        enableSessionTracking: Boolean = false,
        enablePush: Boolean = false,
        enablePushBoost: Boolean = false,
        skipUrlHandling: Boolean = false,
        location: CastledConfigs.CastledLocation = CastledConfigs.CastledLocation.US,
        sessionDuration: Long = 900L
    ) {
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