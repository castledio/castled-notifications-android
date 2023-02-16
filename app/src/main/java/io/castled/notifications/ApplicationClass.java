package io.castled.notifications;

import android.content.Context;
import android.util.Log;

import androidx.multidex.MultiDex;
import androidx.multidex.MultiDexApplication;

import java.util.List;

import io.castled.CastledNotifications;
import io.castled.inAppTriggerEvents.InAppChannelConfig;
import io.castled.inAppTriggerEvents.event.EventNotification;

public class ApplicationClass extends MultiDexApplication {
    private static final String TAG = "ApplicationClass";
    @Override
    public void onCreate() {

        super.onCreate();

        //FIXME: push notifications is crashing here. Tracking in https://github.com/dheerajbhaskar/castled-notifications-android/issues/16
//        CastledNotifications.initialize(this, "829c38e2e359d94372a2e0d35e1f74df");
//        CastledNotifications.setUserId("dheeraj.osw@gmail.com");

        InAppChannelConfig inAppConfig = InAppChannelConfig.builder().enable(true).fetchFromCloudInterval(45).build();
        String t = CastledNotifications.initialize(ApplicationClass.this, "829c38e2e359d94372a2e0d35e1f74df", inAppConfig);
//        Log.d(TAG, "CastledNotifications.initialize: " + t);
    }

    @Override
    protected void attachBaseContext(Context base) {

        super.attachBaseContext(base);
        MultiDex.install(base);
    }
}