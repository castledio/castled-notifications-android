package io.castled.android.demoapp

import android.content.Context
import androidx.multidex.MultiDex
import androidx.multidex.MultiDexApplication
import io.castled.android.notifications.CastledConfigs
import io.castled.android.notifications.CastledNotifications
import io.castled.android.notifications.CastledPushNotificationListener
import io.castled.android.notifications.inapp.CastledInappNotificationListener
import io.castled.android.notifications.inbox.CastledInboxListener
import io.castled.android.notifications.logger.CastledLogger
import io.castled.android.notifications.push.models.CastledActionContext
import io.castled.android.notifications.push.models.CastledPushMessage

class MyApplicationClass : MultiDexApplication() {
    override fun onCreate() {
        super.onCreate()

        // SDK initialization
        CastledNotifications.initialize(
            this,
            CastledConfigs.Builder()
                .appId("e8a4f68bfb6a58b40a77a0e6150eca0b")
                .location(CastledConfigs.CastledLocation.TEST)
                .enablePush(true)
                .enablePushBoost(true)
                .enableAppInbox(true)
                .enableInApp(true)
                .enableSessionTracking(true)
//                .skipUrlHandling(true)
                .enableTracking(true)
                .inAppFetchIntervalSec(60)
                .sessionTimeOutSec(10)
                .build()
        )

        // User-id needs to set after login flow of your app is complete
        CastledNotifications.setUserId(this, "antony@castled.io")

        // Listening to push notification events
        CastledNotifications.subscribeToPushNotificationEvents(object :
            CastledPushNotificationListener {
            val logger = CastledLogger.getInstance("CastledNotifications-DemoApp")

            override fun onCastledPushReceived(pushMessage: CastledPushMessage) {
                logger.debug("Received push message with id: ${pushMessage.notificationId}")
            }

            override fun onCastledPushClicked(
                pushMessage: CastledPushMessage,
                actionContext: CastledActionContext
            ) {
                logger.debug("User clicked push message with id: ${pushMessage.notificationId} $actionContext")
            }

            override fun onCastledPushDismissed(pushMessage: CastledPushMessage) {
                logger.debug("Dismissed push message with id: ${pushMessage.notificationId} ")
            }
        })

        // Listening to inapp notification clicks
        CastledNotifications.subscribeToInappEvents(object :
            CastledInappNotificationListener {
            val logger = CastledLogger.getInstance("CastledInappNotifications-DemoApp")
            override fun onCastledInappClicked(actionContext: CastledActionContext) {
                logger.debug("Inapp Notificaiton clicked: ${actionContext}")
            }

        })

        // Listening to inbox notification clicks
        CastledNotifications.subscribeToInboxEvents(object :
            CastledInboxListener {
            val logger = CastledLogger.getInstance("CastledInboxNotifications-DemoApp")
            override fun onCastledInboxClicked(actionContext: CastledActionContext) {
                logger.debug("Inbox item clicked: ${actionContext}")
            }

        })
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        MultiDex.install(base)
    }
}