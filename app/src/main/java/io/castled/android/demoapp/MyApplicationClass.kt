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
            CastledConfigs.Builder().apiKey("718c38e2e359d94367a2e0d35e1fd4df")
                .location(CastledConfigs.CastledLocation.US).enablePush(true).enableInApp(false)
                .enableTracking(true).inAppFetchIntervalSec(10).xiaomiAppId("2882303761521565034")
                .xiaomiAppKey("5382156577034").build()
        )

//        // User identification
        CastledNotifications.setUserId(
            this, "antony@castled.io", this::onSuccess, this::onError
        )
//        CastledNotifications.setSecureUserId(
//            this,
//            "antony@castled.io",
//            "90bf526ec70e570e16e82c8c788bc39aadbde7013a5ad85cfbfeaf0774ee0fdf",
//            this::onSuccess,
//            this::onError
//        )

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