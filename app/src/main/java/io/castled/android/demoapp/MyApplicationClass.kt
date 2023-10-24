package io.castled.android.demoapp

import android.content.Context
import androidx.multidex.MultiDex
import androidx.multidex.MultiDexApplication
import io.castled.android.notifications.CastledConfigs
import io.castled.android.notifications.CastledNotifications

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
                .enableInApp(true)
                .enableAppInbox(true)
                .enableTracking(true)
                .inAppFetchIntervalSec(60)
                .xiaomiAppId("2882303761522058544")
                .xiaomiAppKey("5302205887544")
                .xiaomiRegion(CastledConfigs.XiaomiRegion.India)
                .build()
        )

        // User-id needs to set after login flow of your app is complete
        CastledNotifications.setUserId(this, "antony@castled.io")
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        MultiDex.install(base)
    }
}