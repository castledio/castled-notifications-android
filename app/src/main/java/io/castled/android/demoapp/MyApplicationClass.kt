package io.castled.android.demoapp

import android.content.Context
import androidx.multidex.MultiDex
import androidx.multidex.MultiDexApplication
import io.castled.android.notifications.CastledConfigs
import io.castled.android.notifications.CastledNotifications
import io.castled.android.notifications.CastledPushNotificationListener
import io.castled.android.notifications.logger.CastledLogger
import io.castled.android.notifications.push.extensions.getNotificationDisplayId
import io.castled.android.notifications.push.models.CastledActionContext
import io.castled.android.notifications.push.models.CastledPushMessage

class MyApplicationClass : MultiDexApplication() {
    override fun onCreate() {
        super.onCreate()

        // SDK initialization
        CastledNotifications.initialize(
            this,
            CastledConfigs.Builder()
                .appId("718c38e2e359d94367a2e0d35e1fd4df")
                .location(CastledConfigs.CastledLocation.US)
                .enablePush(true)
                .enablePushBoost(true)
                .enableAppInbox(true)
                .enableInApp(true)
                .enableSessionTracking(true)
                .enableTracking(true)
                .inAppFetchIntervalSec(60)
                .sessionTimeOutSec(30)
                .build()
        )

        // User-id needs to set after login flow of your app is complete
        CastledNotifications.setUserId(this, "antony@castled.io")

        // Listening to push notification events
        CastledNotifications.subscribeToPushNotificationEvents(object :
            CastledPushNotificationListener {
            val logger = CastledLogger.getInstance("CastledNotifications-DemoApp")

            override fun onCastledPushReceived(pushMessage: CastledPushMessage) {
                logger.debug("Received push message with id: ${pushMessage.getNotificationDisplayId()}")
            }

            override fun onCastledPushClicked(
                pushMessage: CastledPushMessage,
                actionContext: CastledActionContext
            ) {
                logger.debug("User clicked push message with id: ${pushMessage.getNotificationDisplayId()}")
            }

            override fun onCastledPushDismissed(pushMessage: CastledPushMessage) {
                logger.debug("Dismissed push message with id: ${pushMessage.getNotificationDisplayId()}")
            }
        })
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        MultiDex.install(base)
    }
}