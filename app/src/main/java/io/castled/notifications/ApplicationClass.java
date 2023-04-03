package io.castled.notifications;

import android.content.Context;

import androidx.multidex.MultiDex;
import androidx.multidex.MultiDexApplication;

import java.util.List;

import io.castled.CastledNotifications;
import io.castled.inAppTriggerEvents.InAppChannelConfig;

public class ApplicationClass extends MultiDexApplication {
    @Override
    public void onCreate() {

        super.onCreate();

        InAppChannelConfig inAppConfig = InAppChannelConfig.builder()
                .enable(true)
                .fetchFromCloudInterval(30)
                .build();

        CastledNotifications.initialize(ApplicationClass.this, "829c38e2e359d94372a2e0d35e1f74df", List.of(inAppConfig));

        //TODO: close gitHub-> V1 pr bugs - set 1 #45(13. setUserId call happens after init and not before. Also userId should be stored in pref store. You can refer the push code)
        CastledNotifications.setUserId("1987frank@gmail.com");
    }

    @Override
    protected void attachBaseContext(Context base) {

        super.attachBaseContext(base);
        MultiDex.install(base);
    }
}