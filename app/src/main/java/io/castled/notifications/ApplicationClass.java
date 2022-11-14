package io.castled.notifications;

import android.content.Context;

import androidx.multidex.MultiDex;
import androidx.multidex.MultiDexApplication;

public class ApplicationClass extends MultiDexApplication {

    @Override
    public void onCreate() {

        super.onCreate();

        CastledNotifications.initialize(this, "test-101");
        CastledNotifications.setUserId("arun-android@castled.io");
    }

    @Override
    protected void attachBaseContext(Context base) {

        super.attachBaseContext(base);
        MultiDex.install(base);
    }
}