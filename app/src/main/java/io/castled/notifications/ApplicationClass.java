package io.castled.notifications;

import android.content.Context;

import androidx.multidex.MultiDex;
import androidx.multidex.MultiDexApplication;

import java.util.List;

import io.castled.inAppTriggerEvents.CastledNotifications;
import io.castled.inAppTriggerEvents.InAppChannelConfig;
import io.castled.inAppTriggerEvents.event.EventNotification;

public class ApplicationClass extends MultiDexApplication {

    @Override
    public void onCreate() {

        super.onCreate();

        //FIXME: push notifications is crashing here. Tracking in https://github.com/dheerajbhaskar/castled-notifications-android/issues/16
//        CastledNotifications.initialize(this, "829c38e2e359d94372a2e0d35e1f74df");
//        CastledNotifications.setUserId("dheeraj.osw@gmail.com");

        EventNotification.getInstance().triggerEventsFetchFromCloudSetFrequencyInSeconds(30);

//        EventNotification.getInstance().initialize(getApplicationContext());

        EventNotification.getInstance().initialize(ApplicationClass.this);


        InAppChannelConfig inAppConfig = InAppChannelConfig.builder().enable(true).fetchFromCloudInterval(45).build();
        CastledNotifications.initialize(ApplicationClass.this, "INSTANCE_ID", List.of(inAppConfig));


//        registerActivityLifecycleCallbacks(new ActivityLifecycleObserver());

//        EventNotification.getInstance().registerCallback(registerActivityLifecycleCallbacks())
    }

    @Override
    protected void attachBaseContext(Context base) {

        super.attachBaseContext(base);
        MultiDex.install(base);
    }
}