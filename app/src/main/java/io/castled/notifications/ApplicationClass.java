package io.castled.notifications;

import android.content.Context;

import androidx.multidex.MultiDex;
import androidx.multidex.MultiDexApplication;

import io.castled.inAppTriggerEvents.event.EventNotification;

public class ApplicationClass extends MultiDexApplication {

    @Override
    public void onCreate() {

        super.onCreate();

        CastledNotifications.initialize(this, "YOUR_INSTANCE_ID");
        CastledNotifications.setUserId("USER_ID");

        EventNotification.getInstance().triggerEventsFetchFromCloudSetFrequencyInSeconds(45);

//        EventNotification.getInstance().initialize(getApplicationContext());

        EventNotification.getInstance().initialize(ApplicationClass.this);






//        registerActivityLifecycleCallbacks(new ActivityLifecycleObserver());

//        EventNotification.getInstance().registerCallback(registerActivityLifecycleCallbacks())
    }

    @Override
    protected void attachBaseContext(Context base) {

        super.attachBaseContext(base);
        MultiDex.install(base);
    }
}