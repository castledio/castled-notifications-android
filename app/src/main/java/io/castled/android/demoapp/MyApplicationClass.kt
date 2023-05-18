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
            this, CastledConfigs.Builder()
                .apiKey("qwertyasdfg")
                .location(CastledConfigs.CastledLocation.TEST)
                .enablePush(true)
                .enableInApp(true)
                .inAppFetchIntervalSec(10)
                .xiaomiAppId("2882303761521565034")
                .xiaomiAppKey("5382156577034")
                .build()
        )

        // User identification
        CastledNotifications.setUserId(this, "frank@castled.io", this::onSuccess, this::onError)
    }

    private fun onSuccess() {
        Log.d("MyApp","Castled user identify completed successfully!")
    }

    private fun onError(e: Exception) {
        Log.e("MyApp", "Castled user identify failed!", e)
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        MultiDex.install(base)
    }
}