package io.castled.android.demoapp

import android.content.Context
import android.util.Log
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
            CastledConfigs.Builder().appId("e8a4f68bfb6a58b40a77a0e6150eca0b")
                .location(CastledConfigs.CastledLocation.TEST).enablePush(true).enableInApp(true)
                .enableAppInbox(true).enableTracking(true).inAppFetchIntervalSec(10)
                .xiaomiAppId("2882303761521565034")
                .xiaomiAppKey("5382156577034").build()
        )
        CastledNotifications.setUserId(this, "antony@castled.io")
    }

    private fun onSuccess() {
        Log.d("MyApp", "Castled user identify completed successfully!")
    }

    private fun onError(e: Exception) {
        Log.e("MyApp", "Castled user identify failed!", e)
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        MultiDex.install(base)
    }
}