package io.castled.notifications;

import android.content.Context;
import android.util.Log;

import androidx.multidex.MultiDex;
import androidx.multidex.MultiDexApplication;

import kotlin.Unit;

public class ApplicationClass extends MultiDexApplication {

    private static final String TAG = "CastledNotification-Example";

    @Override
    public void onCreate() {

        super.onCreate();

        // SDK initialization
        CastledNotifications.initialize(this, "qwertyasdfg", new CastledConfigs.Builder()
                .location(CastledConfigs.CastledLocation.TEST)
                .enablePush(true)
                .enableInApp(true)
                .inAppFetchIntervalSec(10)
                .xiaomiAppId("2882303761521565034")
                .xiaomiAppKey("5382156577034")
                .build());

        // User identification
        CastledNotifications.setUserId(this, "frank@castled.io", () -> {
                    onSuccess();
                    return Unit.INSTANCE;
                },
                e -> {
                    onError(e);
                    return Unit.INSTANCE;
                });
    }

    private static void onSuccess() {
        Log.d(TAG, "user-id set successfully!");
    }

    private static void onError(Exception e) {
        Log.e(TAG, "user-id set failed!", e);
    }

    @Override
    protected void attachBaseContext(Context base) {

        super.attachBaseContext(base);
        MultiDex.install(base);
    }
}