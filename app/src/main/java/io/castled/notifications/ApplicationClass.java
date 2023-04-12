package io.castled.notifications;

import android.content.Context;
import android.util.Log;

import androidx.multidex.MultiDex;
import androidx.multidex.MultiDexApplication;

import java.util.List;

import io.castled.notifications.inapp.InAppChannelConfig;
import kotlin.Unit;

public class ApplicationClass extends MultiDexApplication {

    private static String TAG = "CastledNotification-Example";

    @Override
    public void onCreate() {

        super.onCreate();

        InAppChannelConfig inAppConfig = InAppChannelConfig.builder()
                .enable(true)
                .fetchFromCloudInterval(30)
                .build();

        CastledNotifications.initialize(this, "829c38e2e359d94372a2e0d35e1f74df", List.of(inAppConfig));
        CastledNotifications.setUserId(this, "1987frank@gmail.com", () -> {
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
        Log.e(TAG, e.getMessage());
    }

    @Override
    protected void attachBaseContext(Context base) {

        super.attachBaseContext(base);
        MultiDex.install(base);
    }
}