package io.castled.notifications;

import android.content.Context;

import androidx.multidex.MultiDex;
import androidx.multidex.MultiDexApplication;

public class ApplicationClass extends MultiDexApplication {

    @Override
    public void onCreate() {

        super.onCreate();
        CastledNotifications.initialize(this, "app-8316f859-e552-4fed-a103-7957597a093c");
        CastledNotifications.setUserId("arun@castled.io");
        // app-06b7d631-8c99-4622-87dc-8ebf0ae02c86
        // CastledNotifications.initialize(this, "test-3b229735-04ae-455f-a5d4-20a89c092927");
        // CastledNotifications.setUserId("frank@castled.io");
    }

    @Override
    protected void attachBaseContext(Context base) {

        super.attachBaseContext(base);
        MultiDex.install(base);
    }
}