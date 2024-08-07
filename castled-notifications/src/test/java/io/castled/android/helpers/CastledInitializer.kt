package io.castled.android.helpers

import android.app.Application
import io.castled.android.notifications.CastledConfigs
import io.castled.android.notifications.CastledNotifications
import io.castled.android.notifications.environment.CastledEnvironmentConfig

object CastledInitializer {
    var isInitialized = false
    fun initializeCastled(
        application: Application
    ) {
        if (isInitialized) {
            return
        }
        isInitialized = true
        CastledEnvironmentConfig.isTestEnvironment = true
        CastledNotifications.initialize(
            application,
            CastledConfigs.Builder()
                .appId("test_app_id")
                .location(CastledConfigs.CastledLocation.TEST)
                .enableAppInbox(true)
                .enableInApp(true)
                .enableSessionTracking(true)
                .enablePush(false)
                .enablePushBoost(false)
                .skipUrlHandling(false)
                .enableTracking(false)
                .inAppFetchIntervalSec(60)
                .sessionTimeOutSec(2)
                .build()
        )

        // User-id needs to set after login flow of your app is complete
        CastledNotifications.setUserId(application, "antony@castled.io")
    }
}